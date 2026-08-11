package mezz.jei.library.recipes.collect;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link RecipeIngredientRoleMap} contains recipes, IRecipeCategory, and Ingredients for one {@link RecipeIngredientRole}.
 */
public class RecipeIngredientRoleMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final IngredientUidIndex<Set<IRecipeType<?>>> ingredientUidToCategoryMap = new IngredientUidIndex<>();
	private final IngredientUidIndex<Set<IRecipeType<?>>> craftingStationUidToRecipeCategoryMap = new IngredientUidIndex<>();
	private final Comparator<IRecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeIngredientRoleMap(Comparator<IRecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public <T> Stream<IRecipeType<?>> getRecipeTypes(ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		Set<IRecipeType<?>> recipeTypes = ingredientUidToCategoryMap.get(ingredientType, uid).exact();
		Set<IRecipeType<?>> catalystRecipeTypes = craftingStationUidToRecipeCategoryMap.get(ingredientType, uid).exact();
		return Stream.of(recipeTypes, catalystRecipeTypes)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.sorted(recipeTypeComparator);
	}

	public <T> void addCraftingStationForCategory(IRecipeType<?> recipeType, ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		craftingStationUidToRecipeCategoryMap.computeExactIfAbsent(ingredientType, uid, ObjectOpenHashSet::new)
			.add(recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(IRecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return getRecipes(recipeType, ingredient.getType(), ingredient);
	}

	public <T> boolean isCraftingStationForRecipeCategory(IRecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return isCraftingStationForRecipeCategory(recipeType, ingredient.getType(), ingredient);
	}

	private <T, I> List<T> getRecipes(IRecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return List.of();
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object uid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		return recipeTable.get(recipeType, ingredientType, uid);
	}

	private <T, I> boolean isCraftingStationForRecipeCategory(IRecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return false;
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		Set<IRecipeType<?>> craftingStationTypes = craftingStationUidToRecipeCategoryMap.get(ingredientType, exactUid).exact();
		if (craftingStationTypes == null) {
			return false;
		}
		return craftingStationTypes.contains(recipeType);
	}

	public <T> void addRecipe(IRecipeType<T> recipeType, T recipe, IIngredientSupplier ingredientSupplier) {
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
