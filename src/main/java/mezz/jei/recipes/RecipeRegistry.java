package mezz.jei.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.collect.SetMultiMap;
import mezz.jei.collect.Table;
import mezz.jei.config.Constants;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeRegistry implements IRecipeRegistry {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IngredientRegistry ingredientRegistry;
	private final ImmutableMultimap<ResourceLocation, IRecipeHandler<?>> recipeHandlers;
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();
	private final List<IRecipeCategory> recipeCategoriesVisibleCache = new ArrayList<>();
	private final ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, Object> recipeCatalysts;
	private final ImmutableMap<ResourceLocation, IRecipeCategory> recipeCategoriesMap;
	private final Table<ResourceLocation, Object, IRecipeWrapper> wrapperMaps = new Table<>(new HashMap<>(), IdentityHashMap::new); // used when removing recipes
	private final ListMultiMap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = new ListMultiMap<>();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<RecipeRegistryPluginSafeWrapper> plugins = new ArrayList<>();
	private final SetMultiMap<ResourceLocation, IRecipeWrapper> hiddenRecipes = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>())); // recipe category uid key

	public RecipeRegistry(
		List<IRecipeCategory> recipeCategories,
		ListMultiMap<ResourceLocation, IRecipeHandler<?>> recipeHandlers,
		ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers,
		ListMultiMap<ResourceLocation, Object> recipes,
		ListMultiMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
		ListMultiMap<ResourceLocation, Object> recipeCatalysts,
		IngredientRegistry ingredientRegistry,
		List<IRecipeRegistryPlugin> plugins
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		this.ingredientRegistry = ingredientRegistry;
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeHandlers = recipeHandlers.toImmutable();
		this.recipeClickableAreasMap = recipeClickableAreasMap.toImmutable();

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		addRecipes(recipes);

		ImmutableListMultimap.Builder<IRecipeCategory, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
		ImmutableMultimap.Builder<String, ResourceLocation> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();

		for (Map.Entry<ResourceLocation, List<Object>> recipeCatalystEntry : recipeCatalysts.entrySet()) {
			ResourceLocation recipeCategoryUid = recipeCatalystEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<Object> catalystIngredients = recipeCatalystEntry.getValue();
				recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
				for (Object catalystIngredient : catalystIngredients) {
					IIngredientType ingredientType = ingredientRegistry.getIngredientType(catalystIngredient);
					IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
					recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient, ingredientHelper);
					String catalystIngredientKey = getUniqueId(catalystIngredient);
					categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategoryUid);
				}
			}
		}

		this.recipeCatalysts = recipeCatalystsBuilder.build();
		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys = categoriesForRecipeCatalystKeysBuilder.build();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForRecipeCatalystKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipeWrappersForCategories);
		this.plugins.add(new RecipeRegistryPluginSafeWrapper(internalRecipeRegistryPlugin));
		for (IRecipeRegistryPlugin plugin : plugins) {
			this.plugins.add(new RecipeRegistryPluginSafeWrapper(plugin));
		}

		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
	}

	private <T> String getUniqueId(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient);
	}

	private static ImmutableMap<ResourceLocation, IRecipeCategory> buildRecipeCategoriesMap(List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<ResourceLocation, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private void addRecipes(ListMultiMap<ResourceLocation, Object> recipes) {
		Collection<Map.Entry<ResourceLocation, List<Object>>> entries = recipes.entrySet();
		try (ProgressBar progressBar = StartupProgressManager.start("Loading recipes", recipes.getTotalSize())) {
			for (Map.Entry<ResourceLocation, List<Object>> entry : entries) {
				ResourceLocation recipeCategoryUid = entry.getKey();
				for (Object recipe : entry.getValue()) {
					progressBar.step("");
					addRecipe(recipe, recipe.getClass(), recipeCategoryUid);
				}
			}
		}
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		return new Focus<>(mode, ingredient);
	}

	@Override
	@Deprecated
	public void addRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
		if (recipeCategory == null) {
			LOGGER.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		if (hiddenRecipes.contains(recipeCategoryUid, recipe)) {
			unhideRecipe(recipe, recipeCategoryUid);
		} else {
			addRecipe(recipe, recipe, recipeCategory);
		}
	}

	private <T> void addRecipe(T recipe, Class<? extends T> recipeClass, @Nullable ResourceLocation recipeCategoryUid) {
		if (recipeCategoryUid == null) {
			IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, null);
			if (recipeHandler != null) {
				recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			} else {
				LOGGER.error("Could not determine recipe category for recipe: {}", recipeClass);
				return;
			}
		}

		IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeClass, recipeCategoryUid);
		if (recipeWrapper != null) {
			IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
			if (recipeCategory == null) {
				LOGGER.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
				return;
			}

			addRecipe(recipe, recipeWrapper, recipeCategory);
		} else {
			LOGGER.debug("No recipe wrapper for recipe: {}", ErrorUtil.getNameForRecipe(recipe));
		}
	}

	@Override
	@Nullable
	public IRecipeCategory getRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return recipeCategoriesMap.get(recipeCategoryUid);
	}

	private static <T> void logBrokenRecipeHandler(T recipe, IRecipeHandler<T> recipeHandler, Throwable e) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e2) {
			LOGGER.error("Failed recipe.toString", e2);
			recipeInfoBuilder.append(recipe.getClass());
		}
		recipeInfoBuilder.append("\nRecipe Handler failed to create recipe wrapper\n");
		recipeInfoBuilder.append(recipeHandler.getClass());
		LOGGER.error("{}", recipeInfoBuilder.toString(), e);
	}

	private <T> void addRecipe(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		try {
			addRecipeUnchecked(recipe, recipeWrapper, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			LOGGER.error("Found a broken crafting recipe.", e);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			LOGGER.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	private <T> void addRecipeUnchecked(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		wrapperMaps.put(recipeCategory.getUid(), recipe, recipeWrapper);

		Ingredients ingredients = getIngredients(recipeWrapper);

		recipeInputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getInputIngredients());
		recipeOutputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

		recipeWrappersForCategories.put(recipeCategory, recipeWrapper);

		unhideRecipe(recipeWrapper, recipeCategory.getUid());

		recipeCategoriesVisibleCache.clear();
	}

	public Ingredients getIngredients(IRecipeWrapper recipeWrapper) {
		Ingredients ingredients = new Ingredients();
		recipeWrapper.getIngredients(ingredients);
		return ingredients;
	}

	@Override
	public List<IRecipeCategory> getRecipeCategories() {
		if (recipeCategoriesVisibleCache.isEmpty()) {
			for (IRecipeCategory<?> recipeCategory : this.recipeCategories) {
				if (isCategoryVisible(recipeCategory)) {
					recipeCategoriesVisibleCache.add(recipeCategory);
				}
			}
		}
		return recipeCategoriesVisibleCache;
	}

	private boolean isCategoryVisible(IRecipeCategory<?> recipeCategory) {
		if (hiddenRecipeCategoryUids.contains(recipeCategory.getUid())) {
			return false;
		}
		List<Object> allCatalysts = getRecipeCatalysts(recipeCategory, true);
		if (!allCatalysts.isEmpty()) {
			List<Object> visibleCatalysts = getRecipeCatalysts(recipeCategory, false);
			if (visibleCatalysts.isEmpty()) {
				return false;
			}
		}
		return !getRecipeWrappers(recipeCategory).isEmpty();
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(List<ResourceLocation> recipeCategoryUids) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");

		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
			IRecipeCategory<?> recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null && getRecipeCategories().contains(recipeCategory)) {
				builder.add(recipeCategory);
			}
		}
		return builder.build();
	}

	@Nullable
	@Override
	public IRecipeWrapper getRecipeWrapper(Object recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return getRecipeWrapper(recipe, recipe.getClass(), recipeCategoryUid);
	}

	@Nullable
	private <T> IRecipeWrapper getRecipeWrapper(T recipe, Class<? extends T> recipeClass, ResourceLocation recipeCategoryUid) {
		IRecipeWrapper recipeWrapper = wrapperMaps.get(recipeCategoryUid, recipe);
		if (recipeWrapper != null) {
			return recipeWrapper;
		}

		IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, recipeCategoryUid);
		if (recipeHandler != null) {
			try {
				recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
				wrapperMaps.put(recipeCategoryUid, recipe, recipeWrapper);
				return recipeWrapper;
			} catch (RuntimeException | LinkageError e) {
				logBrokenRecipeHandler(recipe, recipeHandler, e);
				return null;
			}
		} else if (recipe instanceof IRecipeWrapper) {
			return (IRecipeWrapper) recipe;
		} else {
			return null;
		}
	}

	@Nullable
	private <T> IRecipeHandler<T> getRecipeHandler(final Class<? extends T> recipeClass, @Nullable ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");

		ImmutableCollection<IRecipeHandler<?>> recipeHandlers;

		if (recipeCategoryUid != null) {
			recipeHandlers = this.recipeHandlers.get(recipeCategoryUid);
		} else {
			recipeHandlers = this.recipeHandlers.values();
		}

		// try to find an exact match. build a list of assignable handlers in case an exact match is not found
		List<IRecipeHandler<T>> assignableHandlers = new ArrayList<>();
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			Class<?> handlerRecipeClass = recipeHandler.getRecipeClass();
			if (handlerRecipeClass.isAssignableFrom(recipeClass)) {
				//noinspection unchecked
				IRecipeHandler<T> assignableRecipeHandler = (IRecipeHandler<T>) recipeHandler;
				if (handlerRecipeClass.equals(recipeClass)) {
					return assignableRecipeHandler;
				}
				// remove any handlers that are super of this one
				assignableHandlers.removeIf(handler -> handler.getRecipeClass().isAssignableFrom(handlerRecipeClass));
				// only add this if it's not a super class of an another assignable handler
				if (assignableHandlers.stream().noneMatch(handler -> handlerRecipeClass.isAssignableFrom(handler.getRecipeClass()))) {
					assignableHandlers.add(assignableRecipeHandler);
				}
			}
		}
		if (assignableHandlers.isEmpty()) {
			return null;
		}
		if (assignableHandlers.size() == 1) {
			return assignableHandlers.get(0);
		}

		// try super classes to get closest match
		Class<?> superClass = recipeClass;
		while (!Object.class.equals(superClass)) {
			superClass = superClass.getSuperclass();
			for (IRecipeHandler<?> recipeHandler : assignableHandlers) {
				if (recipeHandler.getRecipeClass().equals(superClass)) {
					// noinspection unchecked
					return (IRecipeHandler<T>) recipeHandler;
				}
			}
		}

		List<Class<T>> assignableClasses = assignableHandlers.stream().map(IRecipeHandler::getRecipeClass).collect(Collectors.toList());
		LOGGER.warn("Found multiple matching recipe handlers for {}: {}", recipeClass, assignableClasses);
		return assignableHandlers.get(0);
	}

	@Nullable
	public RecipeClickableArea getRecipeClickableArea(GuiContainer gui, double mouseX, double mouseY) {
		ImmutableCollection<RecipeClickableArea> recipeClickableAreas = recipeClickableAreasMap.get(gui.getClass());
		for (RecipeClickableArea recipeClickableArea : recipeClickableAreas) {
			if (recipeClickableArea.checkHover(mouseX, mouseY)) {
				return recipeClickableArea;
			}
		}
		return null;
	}

	@Override
	public <V> List<IRecipeCategory> getRecipeCategories(IFocus<V> focus) {
		focus = Focus.check(focus);

		List<ResourceLocation> allRecipeCategoryUids = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<ResourceLocation> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
			for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
				if (!allRecipeCategoryUids.contains(recipeCategoryUid)) {
					if (hiddenRecipes.containsKey(recipeCategoryUid)) {
						IRecipeCategory<?> recipeCategory = getRecipeCategory(recipeCategoryUid);
						if (recipeCategory != null) {
							List<?> recipeWrappers = getRecipeWrappers(recipeCategory, focus);
							if (!recipeWrappers.isEmpty()) {
								allRecipeCategoryUids.add(recipeCategoryUid);
							}
						}
					} else {
						allRecipeCategoryUids.add(recipeCategoryUid);
					}
				}
			}
		}

		return getRecipeCategories(allRecipeCategoryUids);
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		focus = Focus.check(focus);

		List<T> allRecipeWrappers = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory, focus);
			allRecipeWrappers.addAll(recipeWrappers);
		}

		@SuppressWarnings("unchecked")
		Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
		allRecipeWrappers.removeAll(hidden);

		return allRecipeWrappers;
	}

	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		List<T> allRecipeWrappers = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory);
			allRecipeWrappers.addAll(recipeWrappers);
		}

		@SuppressWarnings("unchecked")
		Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
		allRecipeWrappers.removeAll(hidden);

		return allRecipeWrappers;
	}

	public List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ImmutableList<Object> catalysts = recipeCatalysts.get(recipeCategory);
		if (includeHidden) {
			return catalysts;
		}
		List<Object> visibleCatalysts = new ArrayList<>();
		IngredientFilter ingredientFilter = Internal.getIngredientFilter();
		for (Object catalyst : catalysts) {
			if (ingredientRegistry.isIngredientVisible(catalyst, ingredientFilter)) {
				visibleCatalysts.add(catalyst);
			}
		}
		return visibleCatalysts;
	}

	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory) {
		return getRecipeCatalysts(recipeCategory, false);
	}

	@Override
	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends Container> containerClass = container.getClass();
		IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return recipeTransferHandler;
		}

		return recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}

	@Override
	public <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus<?> focus) {
		focus = Focus.check(focus);
		RecipeLayout recipeLayout = RecipeLayout.create(-1, recipeCategory, recipeWrapper, focus, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout;
	}

	@Override
	public void hideRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		hiddenRecipes.put(recipeCategoryUid, recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void unhideRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		hiddenRecipes.remove(recipeCategoryUid, recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			throw new IllegalArgumentException("Unknown recipe category: " + recipeCategoryUid);
		}
		hiddenRecipeCategoryUids.add(recipeCategoryUid);
		recipeCategoriesVisibleCache.remove(recipeCategory);
	}

	@Override
	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		if (!recipeCategoriesMap.containsKey(recipeCategoryUid)) {
			throw new IllegalArgumentException("Unknown recipe category: " + recipeCategoryUid);
		}
		hiddenRecipeCategoryUids.remove(recipeCategoryUid);
		recipeCategoriesVisibleCache.clear();
	}
}
