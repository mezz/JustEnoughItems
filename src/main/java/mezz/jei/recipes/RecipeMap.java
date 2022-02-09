package mezz.jei.recipes;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.SetMultiMap;
import mezz.jei.ingredients.IIngredientSupplier;
import net.minecraft.resources.ResourceLocation;
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
	private final SetMultiMap<String, ResourceLocation> ingredientUidToCategoryMap = new SetMultiMap<>();
	private final SetMultiMap<String, ResourceLocation> categoryCatalystUidToRecipeCategoryMap = new SetMultiMap<>();
	private final Comparator<ResourceLocation> recipeCategoryUidComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeMap(Comparator<ResourceLocation> recipeCategoryUidComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeCategoryUidComparator = recipeCategoryUidComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public List<ResourceLocation> getRecipeCategories(String ingredientUid) {
		Collection<ResourceLocation> recipeCategoryUids = ingredientUidToCategoryMap.get(ingredientUid);
		Collection<ResourceLocation> catalystRecipeCategoryUids = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return Stream.concat(recipeCategoryUids.stream(), catalystRecipeCategoryUids.stream())
			.sorted(recipeCategoryUidComparator)
			.toList();
	}

	public void addCatalystForCategory(IRecipeCategory<?> recipeCategory, String ingredientUid) {
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		categoryCatalystUidToRecipeCategoryMap.put(ingredientUid, recipeCategoryUid);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, String ingredientUid) {
		return recipeTable.get(recipeCategory, ingredientUid);
	}

	public <T> boolean isCatalystForRecipeCategory(IRecipeCategory<T> recipeCategory, String ingredientUid) {
		Collection<ResourceLocation> catalystCategories = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return catalystCategories.contains(recipeCategory.getUid());
	}

	public <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IIngredientSupplier ingredientSupplier) {
		ingredientSupplier.getIngredientTypes(this.role)
			.forEach(ingredientType ->
				addRecipe(recipe, recipeCategory, ingredientSupplier, ingredientType)
			);
	}

	private <T, V> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IIngredientSupplier ingredientSupplier, IIngredientType<V> ingredientType) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		List<String> ingredientUids = ingredientSupplier.getIngredientStream(ingredientType, this.role)
			.filter(ingredientHelper::isValidIngredient)
			.map(i -> ingredientHelper.getUniqueId(i, UidContext.Recipe))
			.distinct()
			.toList();

		if (!ingredientUids.isEmpty()) {
			ResourceLocation recipeCategoryUid = recipeCategory.getUid();
			for (String uid : ingredientUids) {
				ingredientUidToCategoryMap.put(uid, recipeCategoryUid);
			}
			recipeTable.add(recipe, recipeCategory, ingredientUids);
		}
	}
}
