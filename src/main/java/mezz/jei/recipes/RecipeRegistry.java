package mezz.jei.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

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
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
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
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class RecipeRegistry implements IRecipeRegistry {
	private final IngredientRegistry ingredientRegistry;
	@Deprecated
	private final ImmutableList<IRecipeHandler> unsortedRecipeHandlers;
	private final ImmutableMultimap<String, IRecipeHandler> recipeHandlers;
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final Set<String> hiddenRecipeCategoryUids = new HashSet<>();
	private final List<IRecipeCategory> recipeCategoriesVisibleCache = new ArrayList<>();
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, Object> recipeCatalysts;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final RecipeCategoryComparator recipeCategoryComparator;
	private final Table<String, Object, IRecipeWrapper> wrapperMaps = new Table<>(new HashMap<>(), IdentityHashMap::new); // used when removing recipes
	private final ListMultiMap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = new ListMultiMap<>();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<RecipeRegistryPluginSafeWrapper> plugins = new ArrayList<>();
	private final SetMultiMap<String, IRecipeWrapper> hiddenRecipes = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>())); // recipe category uid key

	public RecipeRegistry(
		List<IRecipeCategory> recipeCategories,
		List<IRecipeHandler> unsortedRecipeHandlers,
		ListMultiMap<String, IRecipeHandler> recipeHandlers,
		ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers,
		List<Object> unsortedRecipes,
		ListMultiMap<String, Object> recipes,
		ListMultiMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
		ListMultiMap<String, Object> recipeCatalysts,
		IngredientRegistry ingredientRegistry,
		List<IRecipeRegistryPlugin> plugins
	) {
		this.ingredientRegistry = ingredientRegistry;
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeHandlers = recipeHandlers.toImmutable();
		this.unsortedRecipeHandlers = buildRecipeHandlersList(unsortedRecipeHandlers);
		this.recipeClickableAreasMap = recipeClickableAreasMap.toImmutable();

		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
		this.recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		addRecipes(unsortedRecipes, recipes);

		ImmutableListMultimap.Builder<IRecipeCategory, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
		ImmutableMultimap.Builder<String, String> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();

		for (Map.Entry<String, List<Object>> recipeCatalystEntry : recipeCatalysts.entrySet()) {
			String recipeCategoryUid = recipeCatalystEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<Object> catalystIngredients = recipeCatalystEntry.getValue();
				recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
				for (Object catalystIngredient : catalystIngredients) {
					IIngredientType ingredientType = ingredientRegistry.getIngredientType(catalystIngredient);
					@SuppressWarnings("unchecked")
					IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
					//noinspection unchecked
					recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient, ingredientHelper);
					String catalystIngredientKey = getUniqueId(catalystIngredient);
					categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategoryUid);
				}
			}
		}

		this.recipeCatalysts = recipeCatalystsBuilder.build();
		ImmutableMultimap<String, String> categoriesForRecipeCatalystKeys = categoriesForRecipeCatalystKeysBuilder.build();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForRecipeCatalystKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipeWrappersForCategories);
		this.plugins.add(new RecipeRegistryPluginSafeWrapper(internalRecipeRegistryPlugin));
		for (IRecipeRegistryPlugin plugin : plugins) {
			this.plugins.add(new RecipeRegistryPluginSafeWrapper(plugin));
		}
	}

	private <T> String getUniqueId(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient);
	}

	private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private static ImmutableList<IRecipeHandler> buildRecipeHandlersList(List<IRecipeHandler> recipeHandlers) {
		ImmutableList.Builder<IRecipeHandler> listBuilder = ImmutableList.builder();
		Set<Class> recipeHandlerClasses = new HashSet<>();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass = recipeHandler.getRecipeClass();
			if (recipeHandlerClasses.contains(recipeClass)) {
				Log.get().error("A Recipe Handler has already been registered for this recipe class: {}", recipeClass.getName());
				continue;
			}

			recipeHandlerClasses.add(recipeClass);
			listBuilder.add(recipeHandler);
		}
		return listBuilder.build();
	}

	private void addRecipes(List<Object> unsortedRecipes, ListMultiMap<String, Object> recipes) {
		Collection<Map.Entry<String, List<Object>>> entries = recipes.entrySet();
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Loading recipes", recipes.getTotalSize() + unsortedRecipes.size());
		for (Map.Entry<String, List<Object>> entry : entries) {
			String recipeCategoryUid = entry.getKey();
			for (Object recipe : entry.getValue()) {
				progressBar.step("");
				addRecipe(recipe, recipe.getClass(), recipeCategoryUid);
			}
		}
		for (Object recipe : unsortedRecipes) {
			progressBar.step("");
			addRecipe(recipe, recipe.getClass(), null);
		}
		ProgressManager.pop(progressBar);
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		return new Focus<>(mode, ingredient);
	}

	@Override
	@Deprecated
	public void addRecipe(Object recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.assertMainThread();

		addRecipe(recipe, recipe.getClass(), null);
	}

	@Override
	@Deprecated
	public void addRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		if (hiddenRecipes.contains(recipeCategoryUid, recipe)) {
			unhideRecipe(recipe, recipeCategoryUid);
		} else {
			addRecipe(recipe, recipe, recipeCategory);
		}
	}

	private <T> void addRecipe(T recipe, Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		if (recipeCategoryUid == null) {
			IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, null);
			if (recipeHandler != null) {
				recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			} else {
				Log.get().error("Could not determine recipe category for recipe: {}", recipeClass);
				return;
			}
		}

		IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeClass, recipeCategoryUid);
		if (recipeWrapper != null) {
			IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
			if (recipeCategory == null) {
				Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
				return;
			}

			addRecipe(recipe, recipeWrapper, recipeCategory);
		} else {
			Log.get().debug("No recipe wrapper for recipe: {}", ErrorUtil.getNameForRecipe(recipe));
		}
	}

	@Override
	@Nullable
	public IRecipeCategory getRecipeCategory(String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return recipeCategoriesMap.get(recipeCategoryUid);
	}

	private static <T> void logBrokenRecipeHandler(T recipe, IRecipeHandler<T> recipeHandler, Throwable e) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e2) {
			Log.get().error("Failed recipe.toString", e2);
			recipeInfoBuilder.append(recipe.getClass());
		}
		recipeInfoBuilder.append("\nRecipe Handler failed to create recipe wrapper\n");
		recipeInfoBuilder.append(recipeHandler.getClass());
		Log.get().error("{}", recipeInfoBuilder.toString(), e);
	}

	private <T> void addRecipe(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		try {
			addRecipeUnchecked(recipe, recipeWrapper, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			Log.get().error("Found a broken crafting recipe.", e);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.get().error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	private <T> void addRecipeUnchecked(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		wrapperMaps.put(recipeCategory.getUid(), recipe, recipeWrapper);

		Ingredients ingredients = getIngredients(recipeWrapper);

		//noinspection unchecked
		recipeInputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getInputIngredients());
		//noinspection unchecked
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

	@Deprecated
	@Override
	public void removeRecipe(Object recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.assertMainThread();

		List<IRecipeHandler<Object>> recipeHandlers1 = getRecipeHandlers(recipe.getClass());
		for (IRecipeHandler<Object> recipeHandler : recipeHandlers1) {
			String recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			removeRecipe(recipe, recipeCategoryUid);
		}
	}

	private <T> void removeRecipe(T recipe, String recipeCategoryUid) {
		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		try {
			IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeCategory.getUid());
			if (recipeWrapper != null) {
				hideRecipe(recipeWrapper, recipeCategoryUid);
			}
		} catch (BrokenCraftingRecipeException e) {
			Log.get().error("Found a broken crafting recipe.", e);
		}
	}

	@Override
	@Deprecated
	public void removeRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		hideRecipe(recipe, recipeCategoryUid);
	}

	@Override
	@Deprecated
	public void addSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		ErrorUtil.checkNotEmpty(inputs, "inputs");
		ErrorUtil.checkNotEmpty(output, "output");

		SmeltingRecipe smeltingRecipe = new SmeltingRecipe(inputs, output);
		addRecipe(smeltingRecipe);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		return Internal.getHelpers().getVanillaRecipeFactory().createSmeltingRecipe(inputs, output);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		return Internal.getHelpers().getVanillaRecipeFactory().createAnvilRecipe(leftInput, rightInputs, outputs);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		return Internal.getHelpers().getVanillaRecipeFactory().createBrewingRecipe(ingredients, potionInput, potionOutput);
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
	public List<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");

		List<IRecipeCategory> categories = new ArrayList<>();
		for (String recipeCategoryUid : recipeCategoryUids) {
			IRecipeCategory<?> recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null && getRecipeCategories().contains(recipeCategory)) {
				categories.add(recipeCategory);
			}
		}
		Comparator<IRecipeCategory> comparator = Comparator.comparing(IRecipeCategory::getUid, recipeCategoryComparator);
		categories.sort(comparator);
		return Collections.unmodifiableList(categories);
	}

	@Deprecated
	@Nullable
	@Override
	public <T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass) {
		return getRecipeHandler(recipeClass, null);
	}

	@Nullable
	@Override
	public IRecipeWrapper getRecipeWrapper(Object recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotEmpty(recipeCategoryUid, "recipeCategoryUid");
		return getRecipeWrapper(recipe, recipe.getClass(), recipeCategoryUid);
	}

	@Nullable
	private <T> IRecipeWrapper getRecipeWrapper(T recipe, Class<? extends T> recipeClass, String recipeCategoryUid) {
		IRecipeWrapper recipeWrapper = wrapperMaps.get(recipeCategoryUid, recipe);
		if (recipeWrapper != null) {
			return recipeWrapper;
		}

		IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, recipeCategoryUid);
		if (recipeHandler != null) {
			try {
				if (!recipeHandler.isRecipeValid(recipe)) {
					return null;
				}
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Recipe check crashed", e);
				return null;
			}

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
	private <T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");

		ImmutableCollection<IRecipeHandler> recipeHandlers;

		if (recipeCategoryUid != null) {
			recipeHandlers = this.recipeHandlers.get(recipeCategoryUid);
		} else {
			recipeHandlers = this.recipeHandlers.values();
		}

		// first try to find the exact handler for this recipeClass
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		// fall back on any handler that can accept this recipeClass
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		return null;
	}

	@Deprecated
	private <T> List<IRecipeHandler<T>> getRecipeHandlers(Class<? extends T> recipeClass) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");

		List<IRecipeHandler<T>> recipeHandlers = new ArrayList<>();

		ImmutableCollection<IRecipeHandler> allRecipeHandlers = this.recipeHandlers.values();

		// first try to find the exact handler for this recipeClass
		for (IRecipeHandler<?> recipeHandler : allRecipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}

		// fall back on any handler that can accept this recipeClass
		for (IRecipeHandler<?> recipeHandler : allRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}

		return recipeHandlers;
	}

	@Nullable
	public RecipeClickableArea getRecipeClickableArea(GuiContainer gui, int mouseX, int mouseY) {
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

		List<String> allRecipeCategoryUids = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<String> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
			for (String recipeCategoryUid : recipeCategoryUids) {
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

	@Override
	@Deprecated
	public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, @Nullable IFocus focus) {
		if (focus != null) {
			//noinspection unchecked
			focus = Focus.check(focus);
		}
		List<ItemStack> craftingItems = getCraftingItems(recipeCategory);

		if (focus != null && focus.getMode() == IFocus.Mode.INPUT) {
			Object ingredient = focus.getValue();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(VanillaTypes.ITEM);
				ItemStack matchingStack = ingredientHelper.getMatch(craftingItems, itemStack);
				if (matchingStack != null) {
					return Collections.singletonList(matchingStack);
				}
			}
		}
		return craftingItems;
	}

	@Override
	@Deprecated
	public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		List<Object> objects = getRecipeCatalysts(recipeCategory);
		List<ItemStack> itemStacks = new ArrayList<>();
		for (Object object : objects) {
			if (object instanceof ItemStack) {
				itemStacks.add((ItemStack) object);
			}
		}
		return Collections.unmodifiableList(itemStacks);
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
	public void hideRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		hiddenRecipes.put(recipeCategoryUid, recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void unhideRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		hiddenRecipes.remove(recipeCategoryUid, recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void hideRecipeCategory(String recipeCategoryUid) {
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
	public void unhideRecipeCategory(String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		if (!recipeCategoriesMap.containsKey(recipeCategoryUid)) {
			throw new IllegalArgumentException("Unknown recipe category: " + recipeCategoryUid);
		}
		hiddenRecipeCategoryUids.remove(recipeCategoryUid);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	@Deprecated
	public void hideRecipe(IRecipeWrapper recipe) {
		hideRecipe(recipe, VanillaRecipeCategoryUid.CRAFTING);
	}

	@Override
	@Deprecated
	public void unhideRecipe(IRecipeWrapper recipe) {
		unhideRecipe(recipe, VanillaRecipeCategoryUid.CRAFTING);
	}
}
