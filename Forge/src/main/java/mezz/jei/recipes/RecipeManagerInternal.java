package mezz.jei.recipes;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.ingredients.IIngredientSupplier;
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class RecipeManagerInternal {
	private static final Logger LOGGER = LogManager.getLogger();

	@Unmodifiable
	private final List<IRecipeCategory<?>> recipeCategories;
	private final IngredientVisibility ingredientVisibility;
	private final RegisteredIngredients registeredIngredients;
	private final RecipeTypeDataMap recipeTypeDataMap;
	private final Comparator<IRecipeCategory<?>> recipeCategoryComparator;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;
	private final PluginManager pluginManager;
	private final Set<ResourceLocation> hiddenRecipeCategoryUids = new HashSet<>();

	@Nullable
	@Unmodifiable
	private List<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;

	public RecipeManagerInternal(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<ResourceLocation, ITypedIngredient<?>> recipeCatalysts,
		RegisteredIngredients registeredIngredients,
		List<IRecipeManagerPlugin> plugins,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IngredientVisibility ingredientVisibility
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		this.registeredIngredients = registeredIngredients;
		this.ingredientVisibility = ingredientVisibility;

		Collection<RecipeType<?>> recipeTypes = recipeCategories.stream()
			.<RecipeType<?>>map(IRecipeCategory::getRecipeType)
			.toList();
		Comparator<RecipeType<?>> recipeTypeComparator = recipeCategorySortingConfig.getComparator(recipeTypes);

		this.recipeMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			RecipeMap recipeMap = new RecipeMap(recipeTypeComparator, registeredIngredients, role);
			this.recipeMaps.put(role, recipeMap);
		}

		this.recipeCategoryComparator = Comparator.comparing(IRecipeCategory::getRecipeType, recipeTypeComparator);
		this.recipeCategories = recipeCategories.stream()
			.sorted(this.recipeCategoryComparator)
			.toList();

		RecipeCatalystBuilder recipeCatalystBuilder = new RecipeCatalystBuilder(registeredIngredients, this.recipeMaps.get(RecipeIngredientRole.CATALYST));
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			ResourceLocation recipeCategoryUid = recipeCategory.getRecipeType().getUid();
			if (recipeCatalysts.containsKey(recipeCategoryUid)) {
				List<ITypedIngredient<?>> catalysts = recipeCatalysts.get(recipeCategoryUid);
				recipeCatalystBuilder.addCategoryCatalysts(recipeCategory, catalysts);
			}
		}
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsMap = recipeCatalystBuilder.buildRecipeCategoryCatalysts();
		this.recipeTypeDataMap = new RecipeTypeDataMap(recipeCategories, recipeCategoryCatalystsMap);

		IRecipeManagerPlugin internalRecipeManagerPlugin = new InternalRecipeManagerPlugin(
			registeredIngredients,
			recipeTypeDataMap,
			recipeMaps
		);
		this.pluginManager = new PluginManager(internalRecipeManagerPlugin, plugins);
	}

	@Deprecated
	public <T> void addRecipes(ResourceLocation recipeCategoryUid, Collection<T> recipes) {
		LOGGER.debug("Adding recipes: " + recipeCategoryUid);
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipes, recipeCategoryUid);
		addRecipes(recipeTypeData, recipes);
	}

	public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
		LOGGER.debug("Adding recipes: " + recipeType.getUid());
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipes, recipeType);
		addRecipes(recipeTypeData, recipes);
	}

	private <T> void addRecipes(RecipeTypeData<T> recipeTypeData, Collection<T> recipes) {
		IRecipeCategory<T> recipeCategory = recipeTypeData.getRecipeCategory();
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();

		List<T> addedRecipes = recipes.stream()
			.filter(recipe -> {
				if (hiddenRecipes.contains(recipe) || !recipeCategory.isHandled(recipe)) {
					return false;
				}
				IIngredientSupplier ingredientSupplier = IngredientSupplierHelper.getIngredientSupplier(recipe, recipeCategory, registeredIngredients);
				if (ingredientSupplier == null) {
					return false;
				}
				return addRecipe(recipeCategory, recipe, ingredientSupplier);
			})
			.toList();

		if (!addedRecipes.isEmpty()) {
			recipeTypeData.addRecipes(addedRecipes);
			recipeCategoriesVisibleCache = null;
		}
	}

	private <T> boolean addRecipe(IRecipeCategory<T> recipeCategory, T recipe, IIngredientSupplier ingredientSupplier) {
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		try {
			for (RecipeMap recipeMap : recipeMaps.values()) {
				recipeMap.addRecipe(recipeType, recipe, ingredientSupplier);
			}
			return true;
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
			return false;
		}
	}

	public boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, IFocusGroup focuses) {
		// hide the category if it has been explicitly hidden
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		if (hiddenRecipeCategoryUids.contains(recipeType.getUid())) {
			return true;
		}

		// hide the category if it has catalysts, but they have all been hidden
		if (getRecipeCatalystStream(recipeType, true).findAny().isPresent() &&
			getRecipeCatalystStream(recipeType, false).findAny().isEmpty())
		{
			return true;
		}

		// hide the category if it has no recipes, or if the recipes have all been hidden
		Stream<?> visibleRecipes = getRecipesStream(recipeType, focuses, false);
		return visibleRecipes.findAny().isEmpty();
	}

	@Deprecated
	public Stream<IRecipeCategory<?>> getRecipeCategoriesStream(Collection<ResourceLocation> recipeCategoryUids, IFocusGroup focuses, boolean includeHidden) {
		List<IRecipeCategory<?>> recipeCategories = recipeCategoryUids.stream()
			.map(this.recipeTypeDataMap::get)
			.<IRecipeCategory<?>>map(RecipeTypeData::getRecipeCategory)
			.toList();

		return getRecipeCategoriesCached(recipeCategories, focuses, includeHidden);
	}

	public Stream<IRecipeCategory<?>> getRecipeCategoriesForTypes(Collection<RecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden) {
		List<IRecipeCategory<?>> recipeCategories = recipeTypes.stream()
			.map(this.recipeTypeDataMap::get)
			.<IRecipeCategory<?>>map(RecipeTypeData::getRecipeCategory)
			.toList();

		return getRecipeCategoriesCached(recipeCategories, focuses, includeHidden);
	}

	public RecipeType<?> getTypeForRecipeCategoryUid(ResourceLocation uid) {
		RecipeTypeData<?> categoryData = this.recipeTypeDataMap.get(uid);
		IRecipeCategory<?> recipeCategory = categoryData.getRecipeCategory();
		return recipeCategory.getRecipeType();
	}

	private Stream<IRecipeCategory<?>> getRecipeCategoriesCached(Collection<IRecipeCategory<?>> recipeCategories, IFocusGroup focuses, boolean includeHidden) {
		if (recipeCategories.isEmpty() && focuses.isEmpty() && !includeHidden) {
			if (this.recipeCategoriesVisibleCache == null) {
				this.recipeCategoriesVisibleCache = getRecipeCategoriesUncached(recipeCategories, focuses, includeHidden)
					.toList();
			}
			return this.recipeCategoriesVisibleCache.stream();
		}

		return getRecipeCategoriesUncached(recipeCategories, focuses, includeHidden);
	}

	private Stream<IRecipeCategory<?>> getRecipeCategoriesUncached(Collection<IRecipeCategory<?>> recipeCategories, IFocusGroup focuses, boolean includeHidden) {
		Stream<IRecipeCategory<?>> categoryStream;
		if (focuses.isEmpty()) {
			if (recipeCategories.isEmpty()) {
				// empty focus, empty recipeCategories => get all recipe categories known to JEI
				categoryStream = this.recipeCategories.stream();
			} else {
				// empty focus, non-empty recipeCategories => use the recipeCategories
				categoryStream = recipeCategories.stream()
					.distinct();
			}
		} else {
			// focus => get all recipe categories from plugins with the focus
			categoryStream = this.pluginManager.getRecipeCategoryUids(focuses)
				.map(recipeTypeDataMap::get)
				.map(RecipeTypeData::getRecipeCategory);

			// non-empty recipeCategories => narrow the results to just ones in recipeCategories
			if (!recipeCategories.isEmpty()) {
				categoryStream = categoryStream.filter(recipeCategories::contains);
			}
		}

		if (!includeHidden) {
			categoryStream = categoryStream.filter(c -> !isCategoryHidden(c, focuses));
		}

		return categoryStream.sorted(this.recipeCategoryComparator);
	}

	public <T> Stream<T> getRecipesStream(RecipeType<T> recipeType, IFocusGroup focuses, boolean includeHidden) {
		RecipeTypeData<T> recipeTypeData = this.recipeTypeDataMap.get(recipeType);
		return this.pluginManager.getRecipes(recipeTypeData, focuses, includeHidden);
	}

	public <T> Stream<ITypedIngredient<?>> getRecipeCatalystStream(RecipeType<T> recipeType, boolean includeHidden) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		List<ITypedIngredient<?>> catalysts = recipeTypeData.getRecipeCategoryCatalysts();
		if (includeHidden) {
			return catalysts.stream();
		}
		return catalysts.stream()
			.filter(ingredientVisibility::isIngredientVisible);
	}

	public <T> void hideRecipe(ResourceLocation recipeCategoryUid, T recipe) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.add(recipe);
		recipeCategoriesVisibleCache = null;
	}

	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipes, recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.addAll(recipes);
		recipeCategoriesVisibleCache = null;
	}

	public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipe, recipeCategoryUid);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.remove(recipe);
		recipeCategoriesVisibleCache = null;
	}

	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipes, recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.removeAll(recipes);
		recipeCategoriesVisibleCache = null;
	}

	public void hideRecipeCategory(RecipeType<?> recipeType) {
		hiddenRecipeCategoryUids.add(recipeType.getUid());
		recipeCategoriesVisibleCache = null;
	}

	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		ResourceLocation uid = recipeType.getUid();
		recipeTypeDataMap.validate(uid);
		hiddenRecipeCategoryUids.remove(uid);
		recipeCategoriesVisibleCache = null;
	}

	public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
		hiddenRecipeCategoryUids.add(recipeCategoryUid);
		recipeCategoriesVisibleCache = null;
	}

	public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
		recipeTypeDataMap.validate(recipeCategoryUid);
		hiddenRecipeCategoryUids.remove(recipeCategoryUid);
		recipeCategoriesVisibleCache = null;
	}
}
