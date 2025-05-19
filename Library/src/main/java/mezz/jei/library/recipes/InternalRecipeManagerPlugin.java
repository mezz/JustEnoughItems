package mezz.jei.library.recipes;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.recipes.collect.RecipeIngredientRoleMap;
import mezz.jei.library.recipes.collect.RecipeTypeData;
import mezz.jei.library.recipes.collect.RecipeTypeDataMap;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	private final IIngredientManager ingredientManager;
	private final RecipeTypeDataMap recipeCategoriesMap;
	private final EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> recipeIngredientRoleMaps;

	public InternalRecipeManagerPlugin(
		IIngredientManager ingredientManager,
		RecipeTypeDataMap recipeCategoriesMap,
		EnumMap<RecipeIngredientRole, RecipeIngredientRoleMap> recipeIngredientRoleMaps
	) {
		this.ingredientManager = ingredientManager;
		this.recipeCategoriesMap = recipeCategoriesMap;
		this.recipeIngredientRoleMaps = recipeIngredientRoleMaps;
	}

	@Override
	public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		focus = Focus.checkOne(focus, ingredientManager);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();
		RecipeIngredientRoleMap recipeIngredientRoleMap = this.recipeIngredientRoleMaps.get(role);
		return recipeIngredientRoleMap.getRecipeTypes(ingredient)
			.toList();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {
		focus = Focus.checkOne(focus, ingredientManager);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();

		RecipeIngredientRoleMap recipeIngredientRoleMap = this.recipeIngredientRoleMaps.get(role);
		List<T> recipes = recipeIngredientRoleMap.getRecipes(recipeType, ingredient);
		if (recipeIngredientRoleMap.isCraftingStationForRecipeCategory(recipeType, ingredient)) {
			List<T> recipesForCategory = getRecipes(recipeType);
			return Stream.concat(recipes.stream(), recipesForCategory.stream())
				.distinct()
				.toList();
		}
		return recipes;
	}

	@Override
	public <T> List<T> getRecipes(IRecipeType<T> recipeType) {
		RecipeTypeData<T> recipeTypeData = recipeCategoriesMap.get(recipeType);
		return recipeTypeData.getRecipes();
	}
}
