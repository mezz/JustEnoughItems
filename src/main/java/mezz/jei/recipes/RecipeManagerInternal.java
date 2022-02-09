package mezz.jei.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import mezz.jei.Internal;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RecipeManagerInternal {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ImmutableList<IRecipeCategory<?>> recipeCategories;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();
	private @Nullable ImmutableList<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;
	private final RecipeCategoryDataMap recipeCategoriesDataMap;
	private final Comparator<IRecipeCategory<?>> recipeCategoryComparator;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<RecipeManagerPluginSafeWrapper> plugins = new ArrayList<>();

	public RecipeManagerInternal(
		ImmutableList<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<ResourceLocation, Object> recipeCatalysts,
		IngredientManager ingredientManager,
		ImmutableList<IRecipeManagerPlugin> plugins,
		RecipeCategorySortingConfig recipeCategorySortingConfig
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		Collection<ResourceLocation> recipeCategoryResourceLocations = recipeCategories.stream()
			.map(IRecipeCategory::getUid)
			.toList();
		Comparator<ResourceLocation> recipeCategoryUidComparator = recipeCategorySortingConfig.getComparator(recipeCategoryResourceLocations);
		this.recipeInputMap = new RecipeMap(recipeCategoryUidComparator, ingredientManager);
		this.recipeOutputMap = new RecipeMap(recipeCategoryUidComparator, ingredientManager);
		this.recipeCategoryComparator = Comparator.comparing(IRecipeCategory::getUid, recipeCategoryUidComparator);
		this.recipeCategories = ImmutableList.sortedCopyOf(this.recipeCategoryComparator, recipeCategories);

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
		IRecipeManagerPlugin internalRecipeManagerPlugin = new InternalRecipeManagerPlugin(
			categoriesForRecipeCatalystKeys,
			ingredientManager,
			recipeCategoriesDataMap,
			recipeInputMap,
			recipeOutputMap,
			() -> getRecipeCategoriesStream(null, null, false)
		);
		this.plugins.add(new RecipeManagerPluginSafeWrapper(internalRecipeManagerPlugin));
		for (IRecipeManagerPlugin plugin : plugins) {
			this.plugins.add(new RecipeManagerPluginSafeWrapper(plugin));
		}
	}

	public void addRecipes(Iterable<?> recipes, ResourceLocation recipeCategoryUid) {
		LOGGER.debug("Loading recipes: " + recipeCategoryUid);
		for (Object recipe : recipes) {
			addRecipe(recipe, recipeCategoryUid);
		}
	}

	public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		addRecipe(recipe, recipeCategoryData);
	}

	private <T> void addRecipe(T recipe, RecipeCategoryData<T> recipeCategoryData) {
		IRecipeCategory<T> recipeCategory = recipeCategoryData.getRecipeCategory();
		if (!recipeCategory.isHandled(recipe)) {
			return;
		}
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		if (hiddenRecipes.contains(recipe)) {
			unhideRecipe(recipe, recipeCategory.getUid());
			return;
		}
		try {
			Ingredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);

			recipeInputMap.addRecipe(recipe, recipeCategory, ingredients.getInputIngredients());
			recipeOutputMap.addRecipe(recipe, recipeCategory, ingredients.getOutputIngredients());

			recipeCategoryData.getRecipes().add(recipe);

			recipeCategoriesVisibleCache = null;
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	private <V> boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, @Nullable Focus<V> focus) {
		// hide the category if it has been explicitly hidden
		if (hiddenRecipeCategoryUids.contains(recipeCategory.getUid())) {
			return true;
		}

		// hide the category if it has catalysts and they have all been hidden
		List<Object> allCatalysts = getRecipeCatalysts(recipeCategory, true);
		if (!allCatalysts.isEmpty()) {
			List<Object> visibleCatalysts = getRecipeCatalysts(recipeCategory, false);
			if (visibleCatalysts.isEmpty()) {
				return true;
			}
		}

		// hide the category if it has no recipes, or if the recipes have all been hidden
		Stream<?> visibleRecipes = getRecipesStream(recipeCategory, focus, false);
		return visibleRecipes.findAny().isEmpty();
	}

	public <V> Stream<IRecipeCategory<?>> getRecipeCategoriesStream(@Nullable Collection<ResourceLocation> recipeCategoryUids, @Nullable Focus<V> focus, boolean includeHidden) {
		if (recipeCategoryUids == null && focus == null && !includeHidden) {
			if (this.recipeCategoriesVisibleCache == null) {
				this.recipeCategoriesVisibleCache = getRecipeCategoriesStreamUncached(null, null, false)
					.collect(ImmutableList.toImmutableList());
			}
			return this.recipeCategoriesVisibleCache.stream();
		}

		return getRecipeCategoriesStreamUncached(recipeCategoryUids, focus, includeHidden);
	}

	private <V> Stream<IRecipeCategory<?>> getRecipeCategoriesStreamUncached(@Nullable Collection<ResourceLocation> recipeCategoryUids, @Nullable Focus<V> focus, boolean includeHidden) {
		Stream<IRecipeCategory<?>> categoryStream;
		if (focus == null) {
			if (recipeCategoryUids == null) {
				// null focus, null recipeCategoryUids => get all recipe categories known to JEI
				categoryStream = this.recipeCategories.stream();
			} else {
				// null focus, non-null recipeCategoryUids => get all recipe categories from recipeCategoryUids
				categoryStream = recipeCategoryUids.stream()
					.distinct()
					.map(recipeCategoriesDataMap::get)
					.map(RecipeCategoryData::getRecipeCategory);
			}
		} else {
			// non-null focus => get all recipe categories from plugins with the focus
			Stream<ResourceLocation> uidStream = this.plugins.stream()
				.map(p -> p.getRecipeCategoryUids(focus))
				.flatMap(Collection::stream)
				.distinct();

			// non-null recipeCategoryUids => narrow the results to just ones in recipeCategoryUids
			if (recipeCategoryUids != null) {
				uidStream = uidStream.filter(recipeCategoryUids::contains);
			}

			categoryStream = uidStream
				.map(recipeCategoriesDataMap::get)
				.map(RecipeCategoryData::getRecipeCategory);
		}

		if (!includeHidden) {
			categoryStream = categoryStream.filter(c -> !isCategoryHidden(c, focus));
		}

		return categoryStream.sorted(this.recipeCategoryComparator);
	}

	public <T, V> Stream<T> getRecipesStream(IRecipeCategory<T> recipeCategory, @Nullable Focus<V> focus, boolean includeHidden) {
		Stream<T> recipes = this.plugins.stream()
			.map(p -> getPluginRecipes(p, recipeCategory, focus))
			.flatMap(Collection::stream);

		if (!includeHidden) {
			RecipeCategoryData<T> recipeCategoryData = this.recipeCategoriesDataMap.get(recipeCategory);
			Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
			Predicate<T> notHidden = ((Predicate<T>) hiddenRecipes::contains).negate();

			recipes = recipes.filter(notHidden);
		}

		return recipes;
	}

	private static <T, V> List<T> getPluginRecipes(IRecipeManagerPlugin plugin, IRecipeCategory<T> recipeCategory, @Nullable Focus<V> focus) {
		if (focus != null) {
			return plugin.getRecipes(recipeCategory, focus);
		}
		return plugin.getRecipes(recipeCategory);
	}

	public <T> List<Object> getRecipeCatalysts(IRecipeCategory<T> recipeCategory, boolean includeHidden) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategory);
		ImmutableList<Object> catalysts = recipeCategoryData.getRecipeCatalysts();
		if (includeHidden) {
			return catalysts;
		}
		IngredientFilter ingredientFilter = Internal.getIngredientFilter();
		return catalysts.stream()
			.filter(ingredientFilter::isIngredientVisible)
			.toList();
	}

	public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		hiddenRecipes.add(recipe);
		recipeCategoriesVisibleCache = null;
	}

	public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
		hiddenRecipes.remove(recipe);
		recipeCategoriesVisibleCache = null;
	}

	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		hiddenRecipeCategoryUids.add(recipeCategoryUid);
		recipeCategoriesVisibleCache = null;
	}

	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		recipeCategoriesDataMap.validate(recipeCategoryUid);
		hiddenRecipeCategoryUids.remove(recipeCategoryUid);
		recipeCategoriesVisibleCache = null;
	}
}
