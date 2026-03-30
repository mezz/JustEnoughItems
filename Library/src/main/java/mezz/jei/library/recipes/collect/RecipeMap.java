package mezz.jei.library.recipes.collect;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.library.ingredients.IIngredientSupplier;
import org.jetbrains.annotations.UnmodifiableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 * Optimized with parallel processing support for large datasets.
 */
public class RecipeMap {
	private static final Logger LOGGER = LogManager.getLogger();

	// Threshold for parallel processing
	private static final int PARALLEL_THRESHOLD = 100;

	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final Multimap<Object, RecipeType<?>> ingredientUidToCategoryMap = Multimaps.newSetMultimap(new Object2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
	private final Multimap<Object, RecipeType<?>> categoryCatalystUidToRecipeCategoryMap = Multimaps.newSetMultimap(new Object2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
	private final Comparator<RecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeMap(Comparator<RecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	/**
	 * Get recipe types for an ingredient with parallel sorting.
	 */
	public <T> Stream<RecipeType<?>> getRecipeTypes(ITypedIngredient<T> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		Collection<RecipeType<?>> recipeCategoryUids = ingredientUidToCategoryMap.get(ingredientUid);
		Collection<RecipeType<?>> catalystRecipeCategoryUids = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);

		return Stream.concat(recipeCategoryUids.stream(), catalystRecipeCategoryUids.stream())
			.parallel()
			.distinct()
			.sorted(recipeTypeComparator);
	}

	public <T> void addCatalystForCategory(RecipeType<?> recipeType, ITypedIngredient<T> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		categoryCatalystUidToRecipeCategoryMap.put(ingredientUid, recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		return recipeTable.get(recipeType, ingredientUid);
	}

	public <T> boolean isCatalystForRecipeCategory(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		Collection<RecipeType<?>> catalystCategories = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return catalystCategories.contains(recipeType);
	}

	/**
	 * Add recipe with parallel processing for large ingredient lists.
	 */
	public <T> void addRecipe(RecipeType<T> recipeType, T recipe, IIngredientSupplier ingredientSupplier) {
		Collection<ITypedIngredient<?>> ingredients = ingredientSupplier.getIngredients(this.role);

		// Use parallel processing for large ingredient lists
		if (DebugConfig.isParallelSearchEnabled() && ingredients.size() >= PARALLEL_THRESHOLD) {
			addRecipeParallel(recipeType, recipe, ingredients);
		} else {
			addRecipeSequential(recipeType, recipe, ingredients);
		}
	}

	/**
	 * Sequential recipe addition (small ingredient lists).
	 */
	private <T> void addRecipeSequential(RecipeType<T> recipeType, T recipe, Collection<ITypedIngredient<?>> ingredients) {
		Set<Object> ingredientUids = new HashSet<>();
		for (ITypedIngredient<?> ingredient : ingredients) {
			Object ingredientUid = getIngredientUid(ingredient);
			ingredientUids.add(ingredientUid);
		}

		if (!ingredientUids.isEmpty()) {
			for (Object ingredientUid : ingredientUids) {
				ingredientUidToCategoryMap.put(ingredientUid, recipeType);
			}
			recipeTable.add(recipe, recipeType, ingredientUids);
		}
	}

	/**
	 * Parallel recipe addition (large ingredient lists).
	 */
	private <T> void addRecipeParallel(RecipeType<T> recipeType, T recipe, Collection<ITypedIngredient<?>> ingredients) {
		LOGGER.debug("Adding recipe with {} ingredients using parallel processing", ingredients.size());

		// Extract ingredient UIDs in parallel
		Set<Object> ingredientUids = ingredients.parallelStream()
			.map(this::getIngredientUid)
			.collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));

		if (!ingredientUids.isEmpty()) {
			// Update category map in parallel
			ingredientUids.parallelStream()
				.forEach(uid -> ingredientUidToCategoryMap.put(uid, recipeType));

			// Add to recipe table
			recipeTable.add(recipe, recipeType, ingredientUids);
		}
	}

	public void compact() {
		recipeTable.compact();
	}

	private <T> Object getIngredientUid(ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> type = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);

		if (type instanceof IIngredientTypeWithSubtypes<?, T> ingredientTypeWithSubtypes) {
			if (!ingredientHelper.hasSubtypes(ingredient)) {
				return ingredientTypeWithSubtypes.getBase(ingredient);
			}
		}

		return ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
	}
}
