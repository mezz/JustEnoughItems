package mezz.jei.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.builder.RecipeLayoutBuilder;
import mezz.jei.ingredients.IIngredientSupplier;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RecipeManagerInternal {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ImmutableList<IRecipeCategory<?>> recipeCategories;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();
	private final IngredientVisibility ingredientVisibility;
	private @Nullable ImmutableList<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;
	private final RecipeCategoryDataMap recipeCategoriesDataMap;
	private final Comparator<IRecipeCategory<?>> recipeCategoryComparator;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;
	private final List<RecipeManagerPluginSafeWrapper> plugins = new ArrayList<>();

	public RecipeManagerInternal(
		ImmutableList<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<ResourceLocation, ITypedIngredient<?>> recipeCatalysts,
		IngredientManager ingredientManager,
		ImmutableList<IRecipeManagerPlugin> plugins,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IngredientVisibility ingredientVisibility
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		this.ingredientVisibility = ingredientVisibility;

		Collection<ResourceLocation> recipeCategoryResourceLocations = recipeCategories.stream()
			.map(IRecipeCategory::getUid)
			.toList();
		Comparator<ResourceLocation> recipeCategoryUidComparator = recipeCategorySortingConfig.getComparator(recipeCategoryResourceLocations);

		this.recipeMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			RecipeMap recipeMap = new RecipeMap(recipeCategoryUidComparator, ingredientManager, role);
			this.recipeMaps.put(role, recipeMap);
		}

		this.recipeCategoryComparator = Comparator.comparing(IRecipeCategory::getUid, recipeCategoryUidComparator);
		this.recipeCategories = ImmutableList.sortedCopyOf(this.recipeCategoryComparator, recipeCategories);

		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(ingredientManager, this.recipeMaps.get(RecipeIngredientRole.CATALYST));
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			ResourceLocation recipeCategoryUid = recipeCategory.getUid();
			if (recipeCatalysts.containsKey(recipeCategoryUid)) {
				List<ITypedIngredient<?>> catalysts = recipeCatalysts.get(recipeCategoryUid);
				recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, catalysts);
			}
		}
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsMap = recipeCatalystBuilder.buildRecipeCategoryCatalysts();
		this.recipeCategoriesDataMap = new RecipeCategoryDataMap(recipeCategories, recipeCategoryCatalystsMap);

		IRecipeManagerPlugin internalRecipeManagerPlugin = new InternalRecipeManagerPlugin(
			ingredientManager,
			recipeCategoriesDataMap,
			recipeMaps
		);
		this.plugins.add(new RecipeManagerPluginSafeWrapper(internalRecipeManagerPlugin));
		for (IRecipeManagerPlugin plugin : plugins) {
			this.plugins.add(new RecipeManagerPluginSafeWrapper(plugin));
		}
	}

	public <T> void addRecipes(Collection<T> recipes, ResourceLocation recipeCategoryUid) {
		LOGGER.debug("Loading recipes: " + recipeCategoryUid);

		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipes, recipeCategoryUid);
		IRecipeCategory<T> recipeCategory = recipeCategoryData.getRecipeCategory();
		Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();

		List<T> addedRecipes = recipes.stream()
			.filter(recipe -> {
				if (hiddenRecipes.contains(recipe) || !recipeCategory.isHandled(recipe)) {
					return false;
				}
				IIngredientSupplier ingredientSupplier = getIngredientSupplier(recipe, recipeCategory);
				if (ingredientSupplier == null) {
					return false;
				}
				return addRecipe(recipe, recipeCategory, ingredientSupplier);
			})
			.toList();

		if (!addedRecipes.isEmpty()) {
			recipeCategoryData.addRecipes(addedRecipes);
			recipeCategoriesVisibleCache = null;
		}
	}

	@SuppressWarnings({"removal"})
	@Nullable
	public static <T> IIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory) {
		try {
			IIngredientManager ingredientManager = Internal.getIngredientManager();
			RecipeLayoutBuilder builder = new RecipeLayoutBuilder(ingredientManager);
			recipeCategory.setRecipe(builder, recipe, List.of());
			if (builder.isUsed()) {
				return builder;
			}
		} catch (RuntimeException | LinkageError e) {
			String recipeName = ErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder: {}\n", recipeName, e);
		}

		try {
			Ingredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			return ingredients;
		} catch (RuntimeException | LinkageError e) {
			String recipeName = ErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to set Ingredients: {}\n", recipeName, e);
		}

		return null;
	}

	private <T> boolean addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IIngredientSupplier ingredientSupplier) {
		try {
			for (RecipeMap recipeMap : recipeMaps.values()) {
				recipeMap.addRecipe(recipe, recipeCategory, ingredientSupplier);
			}
			return true;
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
			return false;
		}
	}

	private boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, List<Focus<?>> focuses) {
		// hide the category if it has been explicitly hidden
		if (hiddenRecipeCategoryUids.contains(recipeCategory.getUid())) {
			return true;
		}

		// hide the category if it has catalysts, but they have all been hidden
		if (getRecipeCatalystStream(recipeCategory, true).findAny().isPresent() &&
			getRecipeCatalystStream(recipeCategory, false).findAny().isEmpty())
		{
			return true;
		}

		// hide the category if it has no recipes, or if the recipes have all been hidden
		Stream<?> visibleRecipes = getRecipesStream(recipeCategory, focuses, false);
		return visibleRecipes.findAny().isEmpty();
	}

	public Stream<IRecipeCategory<?>> getRecipeCategoriesStream(Collection<ResourceLocation> recipeCategoryUids, List<Focus<?>> focuses, boolean includeHidden) {
		if (recipeCategoryUids.isEmpty() && focuses.isEmpty() && !includeHidden) {
			if (this.recipeCategoriesVisibleCache == null) {
				this.recipeCategoriesVisibleCache = getRecipeCategoriesStreamUncached(List.of(), List.of(), false)
					.collect(ImmutableList.toImmutableList());
			}
			return this.recipeCategoriesVisibleCache.stream();
		}

		return getRecipeCategoriesStreamUncached(recipeCategoryUids, focuses, includeHidden);
	}

	private Stream<IRecipeCategory<?>> getRecipeCategoriesStreamUncached(Collection<ResourceLocation> recipeCategoryUids, List<Focus<?>> focuses, boolean includeHidden) {
		Stream<IRecipeCategory<?>> categoryStream;
		if (focuses.isEmpty()) {
			if (recipeCategoryUids.isEmpty()) {
				// empty focus, empty recipeCategoryUids => get all recipe categories known to JEI
				categoryStream = this.recipeCategories.stream();
			} else {
				// empty focus, non-empty recipeCategoryUids => get all recipe categories from recipeCategoryUids
				categoryStream = recipeCategoryUids.stream()
					.distinct()
					.map(recipeCategoriesDataMap::get)
					.map(RecipeCategoryData::getRecipeCategory);
			}
		} else {
			// focus => get all recipe categories from plugins with the focus
			Stream<ResourceLocation> uidStream = this.plugins.stream()
				.flatMap(p -> focuses.stream().flatMap(focus -> p.getRecipeCategoryUids(focus).stream())
				)
				.distinct();

			// non-empty recipeCategoryUids => narrow the results to just ones in recipeCategoryUids
			if (!recipeCategoryUids.isEmpty()) {
				uidStream = uidStream.filter(recipeCategoryUids::contains);
			}

			categoryStream = uidStream
				.map(recipeCategoriesDataMap::get)
				.map(RecipeCategoryData::getRecipeCategory);
		}

		if (!includeHidden) {
			categoryStream = categoryStream.filter(c -> !isCategoryHidden(c, focuses));
		}

		return categoryStream.sorted(this.recipeCategoryComparator);
	}

	public <T> Stream<T> getRecipesStream(IRecipeCategory<T> recipeCategory, List<Focus<?>> focuses, boolean includeHidden) {
		Stream<T> recipes = this.plugins.stream()
			.flatMap(p -> getPluginRecipeStream(p, recipeCategory, focuses));

		if (!includeHidden) {
			RecipeCategoryData<T> recipeCategoryData = this.recipeCategoriesDataMap.get(recipeCategory);
			Set<T> hiddenRecipes = recipeCategoryData.getHiddenRecipes();
			Predicate<T> notHidden = ((Predicate<T>) hiddenRecipes::contains).negate();

			recipes = recipes.filter(notHidden);
		}

		return recipes;
	}

	private static <T> Stream<T> getPluginRecipeStream(IRecipeManagerPlugin plugin, IRecipeCategory<T> recipeCategory, List<Focus<?>> focuses) {
		if (!focuses.isEmpty()) {
			return focuses.stream()
				.flatMap(focus -> {
					List<T> recipes = plugin.getRecipes(recipeCategory, focus);
					return recipes.stream();
				});
		}
		return plugin.getRecipes(recipeCategory).stream();
	}

	public <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(IRecipeCategory<T> recipeCategory, boolean includeHidden) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesDataMap.get(recipeCategory);
		ImmutableList<ITypedIngredient<?>> catalysts = recipeCategoryData.getRecipeCategoryCatalysts();
		if (includeHidden) {
			return catalysts.stream();
		}
		return catalysts.stream()
			.filter(ingredientVisibility::isIngredientVisible);
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
