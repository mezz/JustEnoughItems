package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.core.collect.SetMultiMap;
import mezz.jei.library.ingredients.IIngredientSupplier;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final SetMultiMap<String, RecipeType<?>> ingredientUidToCategoryMap = new SetMultiMap<>();
	private final SetMultiMap<String, RecipeType<?>> categoryCatalystUidToRecipeCategoryMap = new SetMultiMap<>();
	private final Comparator<RecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeMap(Comparator<RecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public Stream<RecipeType<?>> getRecipeTypes(String ingredientUid) {
		Collection<RecipeType<?>> recipeCategoryUids = ingredientUidToCategoryMap.get(ingredientUid);
		Collection<RecipeType<?>> catalystRecipeCategoryUids = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return Stream.concat(recipeCategoryUids.stream(), catalystRecipeCategoryUids.stream())
			.sorted(recipeTypeComparator);
	}

	public void addCatalystForCategory(RecipeType<?> recipeType, String ingredientUid) {
		categoryCatalystUidToRecipeCategoryMap.put(ingredientUid, recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(RecipeType<T> recipeType, String ingredientUid) {
		return recipeTable.get(recipeType, ingredientUid);
	}

	public <T> boolean isCatalystForRecipeCategory(RecipeType<T> recipeType, String ingredientUid) {
		Collection<RecipeType<?>> catalystCategories = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return catalystCategories.contains(recipeType);
	}

	public <T> void addRecipe(RecipeType<T> recipeType, T recipe, IIngredientSupplier ingredientSupplier) {
		ingredientSupplier.getIngredientTypes(this.role)
			.forEach(ingredientType ->
				addRecipe(recipe, recipeType, ingredientSupplier, ingredientType)
			);
	}

	private <T, V> void addRecipe(T recipe, RecipeType<T> recipeType, IIngredientSupplier ingredientSupplier, IIngredientType<V> ingredientType) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		List<String> ingredientUids = ingredientSupplier.getIngredientStream(ingredientType, this.role)
			.filter(ingredientHelper::isValidIngredient)
			.map(i -> ingredientHelper.getUniqueId(i, UidContext.Recipe))
			.distinct()
			.toList();

		if (!ingredientUids.isEmpty()) {
			for (String uid : ingredientUids) {
				ingredientUidToCategoryMap.put(uid, recipeType);
			}
			recipeTable.add(recipe, recipeType, ingredientUids);
		}
	}
}
