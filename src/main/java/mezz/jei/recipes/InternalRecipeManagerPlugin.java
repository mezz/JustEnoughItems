package mezz.jei.recipes;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	private final IIngredientManager ingredientManager;
	private final RecipeCategoryDataMap recipeCategoriesMap;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;

	public InternalRecipeManagerPlugin(
		IIngredientManager ingredientManager,
		RecipeCategoryDataMap recipeCategoriesMap,
		EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps
	) {
		this.ingredientManager = ingredientManager;
		this.recipeCategoriesMap = recipeCategoriesMap;
		this.recipeMaps = recipeMaps;
	}

	@Override
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		focus = Focus.checkOne(focus);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();
		RecipeMap recipeMap = this.recipeMaps.get(role);
		IIngredientHelper<V> ingredientHelper = this.ingredientManager.getIngredientHelper(ingredient.getType());
		String ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Recipe);

		List<ResourceLocation> recipeCategories = recipeMap.getRecipeCategories(ingredientUid);
		if (focus.getRole() == RecipeIngredientRole.INPUT) {
			List<ResourceLocation> catalystRecipeCategories = this.recipeMaps.get(RecipeIngredientRole.CATALYST)
				.getRecipeCategories(ingredientUid);
			return Stream.concat(
					recipeCategories.stream(),
					catalystRecipeCategories.stream()
				)
				.distinct()
				.toList();
		}

		return recipeCategories;
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		focus = Focus.checkOne(focus);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();

		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient.getType());
		String ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Recipe);

		RecipeMap recipeMap = this.recipeMaps.get(role);
		List<T> recipes = recipeMap.getRecipes(recipeCategory, ingredientUid);
		if (recipeMap.isCatalystForRecipeCategory(recipeCategory, ingredientUid)) {
			List<T> recipesForCategory = getRecipes(recipeCategory);
			return Stream.concat(recipes.stream(), recipesForCategory.stream())
				.distinct()
				.toList();
		}
		return recipes;
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesMap.get(recipeCategory);
		return recipeCategoryData.getRecipes();
	}
}
