package mezz.jei.recipes;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.ingredients.RegisteredIngredients;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	private final RegisteredIngredients registeredIngredients;
	private final RecipeTypeDataMap recipeCategoriesMap;
	private final EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps;

	public InternalRecipeManagerPlugin(
		RegisteredIngredients registeredIngredients,
		RecipeTypeDataMap recipeCategoriesMap,
		EnumMap<RecipeIngredientRole, RecipeMap> recipeMaps
	) {
		this.registeredIngredients = registeredIngredients;
		this.recipeCategoriesMap = recipeCategoriesMap;
		this.recipeMaps = recipeMaps;
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		focus = Focus.checkOne(focus);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();
		RecipeMap recipeMap = this.recipeMaps.get(role);
		IIngredientHelper<V> ingredientHelper = this.registeredIngredients.getIngredientHelper(ingredient.getType());
		String ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Recipe);
		return recipeMap.getRecipeTypes(ingredientUid)
			.map(RecipeType::getUid)
			.toList();
	}

	@Override
	public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		focus = Focus.checkOne(focus);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();
		RecipeMap recipeMap = this.recipeMaps.get(role);
		IIngredientType<V> ingredientType = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = this.registeredIngredients.getIngredientHelper(ingredientType);
		String ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Recipe);
		return recipeMap.getRecipeTypes(ingredientUid)
			.toList();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		focus = Focus.checkOne(focus);
		ITypedIngredient<V> ingredient = focus.getTypedValue();
		RecipeIngredientRole role = focus.getRole();

		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(ingredient.getType());
		String ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Recipe);

		RecipeMap recipeMap = this.recipeMaps.get(role);
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		List<T> recipes = recipeMap.getRecipes(recipeType, ingredientUid);
		if (recipeMap.isCatalystForRecipeCategory(recipeType, ingredientUid)) {
			List<T> recipesForCategory = getRecipes(recipeCategory);
			return Stream.concat(recipes.stream(), recipesForCategory.stream())
				.distinct()
				.toList();
		}
		return recipes;
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		RecipeTypeData<T> recipeTypeData = recipeCategoriesMap.get(recipeType);
		return recipeTypeData.getRecipes();
	}
}
