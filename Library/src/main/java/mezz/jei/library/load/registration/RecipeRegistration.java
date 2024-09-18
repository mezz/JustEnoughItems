package mezz.jei.library.load.registration;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.jei.info.IngredientInfoRecipe;
import mezz.jei.library.recipes.RecipeManagerInternal;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RecipeRegistration implements IRecipeRegistration {
	private final IJeiHelpers jeiHelpers;
	private final IIngredientManager ingredientManager;
	private final RecipeManagerInternal recipeManager;

	public RecipeRegistration(
		IJeiHelpers jeiHelpers,
		IIngredientManager ingredientManager,
		RecipeManagerInternal recipeManager
	) {
		this.jeiHelpers = jeiHelpers;
		this.ingredientManager = ingredientManager;
		this.recipeManager = recipeManager;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return jeiHelpers.getVanillaRecipeFactory();
	}

	@Override
	public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipes, "recipes");
		this.recipeManager.addRecipes(recipeType, recipes);
	}

	@Override
	public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");

		addIngredientInfo(List.of(ingredient), ingredientType, descriptionComponents);
	}

	@Override
	public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");

		IJeiIngredientInfoRecipe recipe = IngredientInfoRecipe.create(ingredientManager, ingredients, ingredientType, descriptionComponents);
		addRecipes(RecipeTypes.INFORMATION, List.of(recipe));
	}
}
