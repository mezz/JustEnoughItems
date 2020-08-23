package mezz.jei.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.helpers.IModIdHelper;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import mezz.jei.Internal;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManager implements IRecipeManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IngredientManager ingredientManager;
	private final ImmutableList<IRecipeCategory<?>> recipeCategories;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();
	private final List<IRecipeCategory<?>> recipeCategoriesVisibleCache = new ArrayList<>();
	private final RecipeCategoryDataMap recipeCategoriesDataMap;
	private final RecipeCategoryComparator recipeCategoryComparator;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<RecipeManagerPluginSafeWrapper> plugins = new ArrayList<>();
	private final IModIdHelper modIdHelper;

	public RecipeManager(
		ImmutableList<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<ResourceLocation, Object> recipes,
		ImmutableListMultimap<ResourceLocation, Object> recipeCatalysts,
		IngredientManager ingredientManager,
		ImmutableList<IRecipeManagerPlugin> plugins,
		IModIdHelper modIdHelper
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		this.ingredientManager = ingredientManager;
		this.modIdHelper = modIdHelper;

		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
		this.recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientManager);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientManager);

		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(ingredientManager);
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			ResourceLocation recipeCategoryUid = recipeCategory.getUid();
			if (recipeCatalysts.containsKey(recipeCategoryUid)) {
				List<Object> catalysts = recipeCatalysts.get(recipeCategoryUid);
				recipeCatalystBuilder.addCatalysts(recipeCategory, catalysts, recipeInputMap);
			}
		}
		ImmutableListMultimap<IRecipeCategory<?>, Object> recipeCatalystsMap = recipeCatalystBuilder.buildRecipeCatalysts();
		this.recipeCategoriesDataMap = new RecipeCategoryDataMap(recipeCategories, recipeCatalystsMap);

		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys = recipeCatalystBuilder.buildCategoriesForRecipeCatalystKeys();
		IRecipeManagerPlugin internalRecipeManagerPlugin = new InternalRecipeManagerPlugin(this, categoriesForRecipeCatalystKeys, ingredientManager, recipeCategoriesDataMap, recipeInputMap, recipeOutputMap);
		this.plugins.add(new RecipeManagerPluginSafeWrapper(internalRecipeManagerPlugin));
		for (IRecipeManagerPlugin plugin : plugins) {
			this.plugins.add(new RecipeManagerPluginSafeWrapper(plugin));
		}

		addRecipes(recipes);
	}

	private void addRecipes(ImmutableListMultimap<ResourceLocation, Object> recipes) {
		Set<ResourceLocation> recipeCategoryUids = recipes.keySet();
		for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
			LOGGER.debug("Loading recipes: " + recipeCategoryUid.toString());
			for (Object recipe : recipes.get(recipeCategoryUid)) {
				addRecipeTyped(recipe, recipeCategoryUid);
			}
		}
	}

	private <T> void addRecipeTyped(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		addRecipe(recipe, recipeCategoryData);
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		return new Focus<>(mode, ingredient);
	}

	@Override
	@Deprecated
	public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();

		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		if (hiddenRecipes.contains(recipe)) {
			unhideRecipe(recipe, recipeCategoryUid);
		} else {
			addRecipe(recipe, recipeCategoryData);
		}
	}

	@Override
	@Nullable
	public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		RecipeCategoryData<?> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategoryUid);
		return recipeCategoryData.getRecipeCategory();
	}

	private <T> void addRecipe(T recipe, RecipeCategoryData<T> recipeCategoryData) {
		IRecipeCategory<T> recipeCategory = recipeCategoryData.getRecipeCategory();
		try {
			Ingredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);

			recipeInputMap.addRecipe(recipe, recipeCategory, ingredients.getInputIngredients());
			recipeOutputMap.addRecipe(recipe, recipeCategory, ingredients.getOutputIngredients());

			recipeCategoryData.getRecipes().add(recipe);

			unhideRecipe(recipe, recipeCategory.getUid());

			recipeCategoriesVisibleCache.clear();
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	@Override
	public List<IRecipeCategory<?>> getRecipeCategories() {
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
	public List<IRecipeCategory<?>> getRecipeCategories(List<ResourceLocation> recipeCategoryUids) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");

		List<IRecipeCategory<?>> categories = new ArrayList<>();
		for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
			RecipeCategoryData<?> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategoryUid);
			IRecipeCategory<?> recipeCategory = recipeCategoryData.getRecipeCategory();
			if (getRecipeCategories().contains(recipeCategory)) {
				categories.add(recipeCategory);
			}
		}
		Comparator<IRecipeCategory<?>> comparator = Comparator.comparing(IRecipeCategory::getUid, recipeCategoryComparator);
		categories.sort(comparator);
		return Collections.unmodifiableList(categories);
	}

	@Override
	public <V> List<IRecipeCategory<?>> getRecipeCategories(IFocus<V> focus) {
		focus = Focus.check(focus);

		List<ResourceLocation> allRecipeCategoryUids = new ArrayList<>();
		for (IRecipeManagerPlugin plugin : this.plugins) {
			List<ResourceLocation> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
			for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
				if (!allRecipeCategoryUids.contains(recipeCategoryUid)) {
					RecipeCategoryData<?> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategoryUid);
					Set<?> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
					if (!hiddenRecipes.isEmpty()) {
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

		List<T> allRecipes = new ArrayList<>();
		for (IRecipeManagerPlugin plugin : this.plugins) {
			List<T> recipes = plugin.getRecipes(recipeCategory, focus);
			allRecipes.addAll(recipes);
		}

		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategory);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		allRecipes.removeAll(hiddenRecipes);

		return allRecipes;
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		List<T> allRecipes = new ArrayList<>();
		for (IRecipeManagerPlugin plugin : this.plugins) {
			List<T> recipes = plugin.getRecipes(recipeCategory);
			allRecipes.addAll(recipes);
		}

		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategory);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		allRecipes.removeAll(hiddenRecipes);

		return allRecipes;
	}

	private <T> List<Object> getRecipeCatalysts(IRecipeCategory<T> recipeCategory, boolean includeHidden) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategory);
		ImmutableList<Object> catalysts = recipeCategoryData.getRecipeCatalysts();
		if (includeHidden) {
			return catalysts;
		}
		List<Object> visibleCatalysts = new ArrayList<>();
		IngredientFilter ingredientFilter = Internal.getIngredientFilter();
		for (Object catalyst : catalysts) {
			if (ingredientManager.isIngredientVisible(catalyst, ingredientFilter)) {
				visibleCatalysts.add(catalyst);
			}
		}
		return visibleCatalysts;
	}

	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
		return getRecipeCatalysts(recipeCategory, false);
	}

	@Override
	public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
		Focus<?> checkedFocus = Focus.check(focus);
		RecipeLayout<?> recipeLayout = RecipeLayout.create(-1, recipeCategory, recipe, checkedFocus, modIdHelper, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout;
	}

	@Override
	public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		hiddenRecipes.add(recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		hiddenRecipes.remove(recipe);
		recipeCategoriesVisibleCache.clear();
	}

	@Override
	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		RecipeCategoryData<?> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategoryUid);
		IRecipeCategory<?> recipeCategory = recipeCategoryData.getRecipeCategory();
		hiddenRecipeCategoryUids.add(recipeCategoryUid);
		recipeCategoriesVisibleCache.remove(recipeCategory);
	}

	@Override
	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
		ErrorUtil.assertMainThread();
		recipeCategoriesDataMap.validate(recipeCategoryUid);
		hiddenRecipeCategoryUids.remove(recipeCategoryUid);
		recipeCategoriesVisibleCache.clear();
	}
}
