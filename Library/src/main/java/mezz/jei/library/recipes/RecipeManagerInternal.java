package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.config.RecipeCategorySortingConfig;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.SimpleIngredientAcceptor;
import mezz.jei.library.recipes.collect.RecipeIngredientRoleMap;
import mezz.jei.library.recipes.collect.RecipeTypeData;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.library.util.RecipeDebugUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RecipeManagerInternal implements IIngredientVisibility.IListener {
	private static final Logger LOGGER = LogManager.getLogger();

	@Unmodifiable
	private final List<IRecipeCategory<?>> recipeCategories;
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final RecipeTypeDataMap recipeTypeDataMap;
	private final Comparator<IRecipeCategory<?>> recipeCategoryComparator;
	private final EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> recipeIngredientRoleMaps;
	private final PluginManager pluginManager;
	private final Set<IRecipeType<?>> hiddenRecipeTypes = new HashSet<>();
	private final IIngredientVisibility ingredientVisibility;

	@Nullable
	@Unmodifiable
	private List<IRecipeCategory<?>> recipeCategoriesVisibleCache = null;

	public RecipeManagerInternal(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> craftingStations,
		IIngredientManagerInternal ingredientManager,
		ContextMap contextMap,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IIngredientVisibility ingredientVisibility
	) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
		this.ingredientVisibility = ingredientVisibility;
		this.ingredientVisibility.registerListener(this);

		Collection<IRecipeType<?>> recipeTypes = recipeCategories.stream()
			.<IRecipeType<?>>map(IRecipeCategory::getRecipeType)
			.toList();
		Comparator<IRecipeType<?>> recipeTypeComparator = recipeCategorySortingConfig.getComparator(recipeTypes);

		this.recipeIngredientRoleMaps = new EnumMap<>(RecipeIngredientRole.class);
		for (RecipeIngredientRole role : RecipeIngredientRole.values()) {
			RecipeIngredientRoleMap recipeIngredientRoleMap = new RecipeIngredientRoleMap(recipeTypeComparator, ingredientManager, role);
			this.recipeIngredientRoleMaps.put(role, recipeIngredientRoleMap);
		}

		this.recipeCategoryComparator = Comparator.comparing(IRecipeCategory::getRecipeType, recipeTypeComparator);
		this.recipeCategories = recipeCategories.stream()
			.sorted(this.recipeCategoryComparator)
			.toList();

		RecipeIngredientRoleMap craftingStationRoleMap = this.recipeIngredientRoleMaps.get(RecipeIngredientRole.CRAFTING_STATION);
		CraftingStationBuilder craftingStationBuilder = new CraftingStationBuilder(craftingStationRoleMap);
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			IRecipeType<?> recipeType = recipeCategory.getRecipeType();
			List<Consumer<IIngredientAcceptor<?>>> categoryCraftingStations = craftingStations.get(recipeType);
			craftingStationBuilder.addCategoryCraftingStations(
				recipeCategory,
				categoryCraftingStations,
				this::resolveCraftingStation
			);
		}
		ImmutableListMultimap<IRecipeCategory<?>, Consumer<IIngredientAcceptor<?>>> craftingStationMap = craftingStationBuilder.build();
		this.recipeTypeDataMap = new RecipeTypeDataMap(recipeCategories, craftingStationMap);

		IRecipeManagerPlugin internalRecipeManagerPlugin = new InternalRecipeManagerPlugin(
			ingredientManager,
			recipeTypeDataMap,
			recipeIngredientRoleMaps
		);
		this.pluginManager = new PluginManager(internalRecipeManagerPlugin);
	}

	public void addPlugins(List<IRecipeManagerPlugin> plugins) {
		this.pluginManager.addAll(plugins);
	}

	public <T> void addRecipes(IRecipeType<T> recipeType, List<T> recipes, ContextMap contextMap) {
		LOGGER.debug("Adding recipes: {}", recipeType);
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		IRecipeCategory<T> recipeCategory = recipeTypeData.getRecipeCategory();
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();

		List<T> addedRecipes = new ArrayList<>(recipes.size());
		for (T recipe : recipes) {
			if (addRecipe(recipeCategory, recipe, hiddenRecipes, contextMap)) {
				addedRecipes.add(recipe);
			}
		}

		if (!addedRecipes.isEmpty()) {
			recipeTypeData.addRecipes(addedRecipes);
			recipeCategoriesVisibleCache = null;
		}
	}

	private <T> boolean addRecipe(IRecipeCategory<T> recipeCategory, T recipe, Set<T> hiddenRecipes, ContextMap contextMap) {
		IRecipeType<T> recipeType = recipeCategory.getRecipeType();
		if (hiddenRecipes.contains(recipe)) {
			if (LOGGER.isDebugEnabled()) {
				String recipeInfo = RecipeDebugUtil.getDebugInfoFromRecipe(recipe, recipeCategory, ingredientManager, contextMap);
				LOGGER.debug("Recipe not added because it is hidden: {}", recipeInfo);
			}
			return false;
		}
		if (!recipeCategory.isHandled(recipe)) {
			if (LOGGER.isDebugEnabled()) {
				String recipeInfo = RecipeDebugUtil.getDebugInfoFromRecipe(recipe, recipeCategory, ingredientManager, contextMap);
				LOGGER.debug("Recipe not added because the recipe category cannot handle it: {}", recipeInfo);
			}
			return false;
		}
		RecipeIngredientSupplier ingredientSupplier = IngredientSupplierHelper.getIngredientSupplier(recipe, recipeCategory, ingredientManager, contextMap);

		try {
			for (RecipeIngredientRoleMap recipeIngredientRoleMap : recipeIngredientRoleMaps.values()) {
				recipeIngredientRoleMap.addRecipe(recipeType, recipe, ingredientSupplier);
			}
			return true;
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = RecipeDebugUtil.getDebugInfoFromRecipe(recipe, recipeCategory, ingredientManager, contextMap);
			LOGGER.error("Found a broken recipe, failed to addRecipe: {}\n", recipeInfo, e);
			return false;
		}
	}

	public boolean isCategoryHidden(IRecipeCategory<?> recipeCategory, IFocusGroup focuses) {
		// hide the category if it has been explicitly hidden
		IRecipeType<?> recipeType = recipeCategory.getRecipeType();
		if (hiddenRecipeTypes.contains(recipeType)) {
			return true;
		}

		// hide the category if it has crafting stations, but they have all been hidden
		if (hasCraftingStations(recipeType, true) &&
			!hasCraftingStations(recipeType, false)
		) {
			return true;
		}

		// hide the category if it has no recipes, or if the recipes have all been hidden
		Stream<?> visibleRecipes = getRecipesStream(recipeType, focuses, false);
		return visibleRecipes.findAny().isEmpty();
	}

	private boolean hasCraftingStations(IRecipeType<?> recipeType, boolean includeHidden) {
		return getCraftingStations(recipeType, includeHidden).findAny().isPresent();
	}

	public Stream<IRecipeCategory<?>> getRecipeCategoriesForTypes(Collection<IRecipeType<?>> recipeTypes, IFocusGroup focuses, boolean includeHidden) {
		List<IRecipeCategory<?>> recipeCategories = recipeTypes.stream()
			.map(this.recipeTypeDataMap::get)
			.<IRecipeCategory<?>>map(RecipeTypeData::getRecipeCategory)
			.toList();

		return getRecipeCategoriesCached(recipeCategories, focuses, includeHidden);
	}

	public <T> IRecipeCategory<T> getRecipeCategory(IRecipeType<T> recipeType) {
		RecipeTypeData<T> value = this.recipeTypeDataMap.get(recipeType);
		return value.getRecipeCategory();
	}

	private Stream<IRecipeCategory<?>> getRecipeCategoriesCached(Collection<IRecipeCategory<?>> recipeCategories, IFocusGroup focuses, boolean includeHidden) {
		if (recipeCategories.isEmpty() && focuses.isEmpty() && !includeHidden) {
			if (this.recipeCategoriesVisibleCache == null) {
				this.recipeCategoriesVisibleCache = getRecipeCategoriesUncached(recipeCategories, focuses, false)
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
			categoryStream = this.pluginManager.getRecipeTypes(focuses)
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

	public <T> Stream<T> getRecipesStream(IRecipeType<T> recipeType, IFocusGroup focuses, boolean includeHidden) {
		RecipeTypeData<T> recipeTypeData = this.recipeTypeDataMap.get(recipeType);
		return this.pluginManager.getRecipes(recipeType, recipeTypeData, focuses, includeHidden);
	}

	public <T> Stream<Consumer<IIngredientAcceptor<?>>> getCraftingStations(IRecipeType<T> recipeType, boolean includeHidden) {
		Stream<Consumer<IIngredientAcceptor<?>>> craftingStations = recipeTypeDataMap.get(recipeType)
			.getCraftingStations()
			.stream();
		if (!includeHidden) {
			craftingStations = craftingStations.filter(craftingStation -> getCraftingStationIngredients(craftingStation, false).findAny().isPresent());
		}
		return craftingStations;
	}

	public Stream<ITypedIngredient<?>> getCraftingStationIngredients(
		Consumer<IIngredientAcceptor<?>> craftingStation,
		boolean includeHidden
	) {
		Stream<ITypedIngredient<?>> ingredients = resolveCraftingStation(craftingStation);
		if (!includeHidden) {
			ingredients = ingredients.filter(this::isCraftingStationVisible);
		}
		return ingredients;
	}

	private Stream<ITypedIngredient<?>> resolveCraftingStation(Consumer<IIngredientAcceptor<?>> craftingStation) {
		SimpleIngredientAcceptor acceptor = new SimpleIngredientAcceptor(ingredientManager, contextMap, RecipeIngredientRole.CRAFTING_STATION);
		craftingStation.accept(acceptor);
		return acceptor.getAllSlotIngredients().stream()
			.map(slotIngredient -> slotIngredient.typedIngredient())
			.<ITypedIngredient<?>>map(ingredientManager::normalizeTypedIngredient);
	}

	private boolean isCraftingStationVisible(ITypedIngredient<?> craftingStation) {
		return ingredientVisibility.isIngredientVisible(craftingStation, UidContext.Recipe);
	}

	@Override
	public <V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible) {
		recipeCategoriesVisibleCache = null;
	}

	@Override
	public <V> void onIngredientsVisibilityChanged(
		Collection<ITypedIngredient<V>> ingredients,
		Collection<UidContext> contexts,
		boolean visible
	) {
		if (contexts.contains(UidContext.Recipe)) {
			recipeCategoriesVisibleCache = null;
		}
	}

	public <T> void hideRecipes(IRecipeType<T> recipeType, Collection<T> recipes) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.addAll(recipes);
		recipeCategoriesVisibleCache = null;
	}

	public <T> void unhideRecipes(IRecipeType<T> recipeType, Collection<T> recipes) {
		RecipeTypeData<T> recipeTypeData = recipeTypeDataMap.get(recipeType);
		Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
		hiddenRecipes.removeAll(recipes);
		recipeCategoriesVisibleCache = null;
	}

	public void hideRecipeCategory(IRecipeType<?> recipeType) {
		hiddenRecipeTypes.add(recipeType);
		recipeCategoriesVisibleCache = null;
	}

	public void unhideRecipeCategory(IRecipeType<?> recipeType) {
		recipeTypeDataMap.validate(recipeType);
		hiddenRecipeTypes.remove(recipeType);
		recipeCategoriesVisibleCache = null;
	}

	public <T> Optional<IRecipeType<T>> getRecipeType(Identifier recipeUid, Class<? extends T> recipeClass) {
		return recipeTypeDataMap.getType(recipeUid, recipeClass);
	}

	public Optional<IRecipeType<?>> getRecipeType(Identifier recipeUid) {
		return recipeTypeDataMap.getType(recipeUid);
	}

	public void compact() {
		recipeIngredientRoleMaps.values().forEach(RecipeIngredientRoleMap::compact);
	}

	public boolean isCraftingStation(IRecipeType<?> recipeType, IFocus<?> focus) {
		RecipeIngredientRoleMap recipeIngredientRoleMap = recipeIngredientRoleMaps.get(focus.getRole());
		return recipeIngredientRoleMap.isCraftingStationForRecipeCategory(recipeType, focus.getTypedValue());
	}
}
