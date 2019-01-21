package mezz.jei.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

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
import mezz.jei.api.recipe.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.collect.SetMultiMap;
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
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();
	private final List<IRecipeCategory> recipeCategoriesVisibleCache = new ArrayList<>();
	private final ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, Object> recipeCatalysts;
	private final ImmutableMap<ResourceLocation, IRecipeCategory> recipeCategoriesMap;
	private final ListMultiMap<IRecipeCategory, Object> recipesForCategories = new ListMultiMap<>();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<RecipeRegistryPluginSafeWrapper> plugins = new ArrayList<>();
	private final SetMultiMap<ResourceLocation, Object> hiddenRecipes = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>())); // recipe category uid key

	public RecipeRegistry(
		List<IRecipeCategory> recipeCategories,
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
		this.recipeClickableAreasMap = recipeClickableAreasMap.toImmutable();

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		addRecipes(recipes);

		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(recipeCategoriesMap, ingredientRegistry);
		recipeCatalystBuilder.addCatalysts(recipeCatalysts, recipeInputMap);

		this.recipeCatalysts = recipeCatalystBuilder.buildRecipeCatalysts();
		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys = recipeCatalystBuilder.buildCategoriesForRecipeCatalystKeys();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForRecipeCatalystKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipesForCategories);
		this.plugins.add(new RecipeRegistryPluginSafeWrapper(internalRecipeRegistryPlugin));
		for (IRecipeRegistryPlugin plugin : plugins) {
			this.plugins.add(new RecipeRegistryPluginSafeWrapper(plugin));
		}

		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
	}

	private static ImmutableMap<ResourceLocation, IRecipeCategory> buildRecipeCategoriesMap(List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<ResourceLocation, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private void addRecipes(ListMultiMap<ResourceLocation, Object> recipes) {
		Set<ResourceLocation> recipeCategoryUids = recipes.keySet();
		try (ProgressBar progressBar = StartupProgressManager.start("Loading recipes", recipeCategoryUids.size())) {
			for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
				progressBar.step(recipeCategoryUid.toString());
				for (Object recipe : recipes.get(recipeCategoryUid)) {
					IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
					if (recipeCategory == null) {
						LOGGER.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
						continue;
					}
					addRecipe(recipe, recipeCategory);
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
	public void addRecipe(Object recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
		if (recipeCategory == null) {
			LOGGER.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}
		Class recipeClass = recipeCategory.getRecipeClass();
		if (!recipeClass.isInstance(recipe)) {
			throw new IllegalArgumentException(recipeCategory.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
		}

		if (hiddenRecipes.contains(recipeCategoryUid, recipe)) {
			unhideRecipe(recipe, recipeCategoryUid);
		} else {
			addRecipe(recipe, recipeCategory);
		}
	}

	@Override
	@Nullable
	public IRecipeCategory getRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		return recipeCategoriesMap.get(recipeCategoryUid);
	}

	private <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory) {
		try {
			Ingredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);

			recipeInputMap.addRecipe(recipe, recipeCategory, ingredients.getInputIngredients());
			recipeOutputMap.addRecipe(recipe, recipeCategory, ingredients.getOutputIngredients());

			recipesForCategories.put(recipeCategory, recipe);

			unhideRecipe(recipe, recipeCategory.getUid());

			recipeCategoriesVisibleCache.clear();
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
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
		return !getRecipes(recipeCategory).isEmpty();
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
							List<?> recipes = getRecipes(recipeCategory, focus);
							if (!recipes.isEmpty()) {
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
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		focus = Focus.check(focus);

		List<T> allRecipeWrappers = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<T> recipeWrappers = plugin.getRecipes(recipeCategory, focus);
			allRecipeWrappers.addAll(recipeWrappers);
		}

		@SuppressWarnings("unchecked")
		Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
		allRecipeWrappers.removeAll(hidden);

		return allRecipeWrappers;
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		List<T> allRecipes = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			List<T> recipes = plugin.getRecipes(recipeCategory);
			allRecipes.addAll(recipes);
		}

		@SuppressWarnings("unchecked")
		Set<T> hidden = (Set<T>) hiddenRecipes.get(recipeCategory.getUid());
		allRecipes.removeAll(hidden);

		return allRecipes;
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
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
		Focus<?> checkedFocus = Focus.check(focus);
		RecipeLayout recipeLayout = RecipeLayout.create(-1, recipeCategory, recipe, checkedFocus, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout;
	}

	@Override
	public void hideRecipe(Object recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		hiddenRecipes.put(recipeCategoryUid, recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void unhideRecipe(Object recipe, ResourceLocation recipeCategoryUid) {
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
