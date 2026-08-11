package mezz.jei.library.recipes.collect;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final IngredientUidIndex<Set<RecipeType<?>>> ingredientUidToCategoryMap = new IngredientUidIndex<>();
	private final IngredientUidIndex<Set<RecipeType<?>>> categoryCatalystUidToRecipeCategoryMap = new IngredientUidIndex<>();
	private final Comparator<RecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeMap(Comparator<RecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public <T> Stream<RecipeType<?>> getRecipeTypes(ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		Set<RecipeType<?>> recipeTypes = ingredientUidToCategoryMap.get(ingredientType, uid).exact();
		Set<RecipeType<?>> catalystRecipeTypes = categoryCatalystUidToRecipeCategoryMap.get(ingredientType, uid).exact();
		return Stream.of(recipeTypes, catalystRecipeTypes)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.sorted(recipeTypeComparator);
	}

	public <T> void addCatalystForCategory(RecipeType<?> recipeType, ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		categoryCatalystUidToRecipeCategoryMap.computeExactIfAbsent(ingredientType, uid, ObjectOpenHashSet::new)
			.add(recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return getRecipes(recipeType, ingredient.getType(), ingredient);
	}

	public <T> boolean isCatalystForRecipeCategory(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return isCatalystForRecipeCategory(recipeType, ingredient.getType(), ingredient);
	}

	private <T, I> List<T> getRecipes(RecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return List.of();
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		return recipeTable.get(recipeType, ingredientType, uid);
	}

	private <T, I> boolean isCatalystForRecipeCategory(RecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return false;
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		Set<RecipeType<?>> catalystTypes = categoryCatalystUidToRecipeCategoryMap.get(ingredientType, exactUid).exact();
		if (catalystTypes == null) {
			return false;
		}
		return catalystTypes.contains(recipeType);
	}

	public <T> void addRecipe(RecipeType<T> recipeType, T recipe, IIngredientSupplier ingredientSupplier) {
		IngredientUidIndex<Boolean> ingredientUids = new IngredientUidIndex<>();
		for (ITypedIngredient<?> ingredient : ingredientSupplier.getIngredients(this.role)) {
			addRecipeIngredient(ingredient, ingredientUids);
		}

		ingredientUids.forEach((ingredientType, uid, buckets) -> {
			if (buckets.exact() != null) {
				ingredientUidToCategoryMap.computeExactIfAbsent(ingredientType, uid, () -> new ObjectOpenHashSet<>(2))
					.add(recipeType);
				recipeTable.addExact(recipe, recipeType, ingredientType, uid);
			}
		});
	}

	public void compact() {
		recipeTable.compact();
	}

	private <T> void addRecipeIngredient(ITypedIngredient<T> typedIngredient, IngredientUidIndex<Boolean> ingredientUids) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		ingredientUids.computeExactIfAbsent(ingredientType, uid, () -> Boolean.TRUE);
	}
}
