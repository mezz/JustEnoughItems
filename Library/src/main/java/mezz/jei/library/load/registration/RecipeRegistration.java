package mezz.jei.library.load.registration;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientInfoRecipe;
import mezz.jei.library.recipes.RecipeManagerInternal;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RecipeRegistration implements IRecipeRegistration {
	private final IJeiHelpers jeiHelpers;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	private final IVanillaRecipeFactory vanillaRecipeFactory;
	private final RecipeManagerInternal recipeManager;

	public RecipeRegistration(
		IJeiHelpers jeiHelpers,
		IIngredientManager ingredientManager,
		IIngredientVisibility ingredientVisibility,
		IVanillaRecipeFactory vanillaRecipeFactory,
		RecipeManagerInternal recipeManager
	) {
		this.jeiHelpers = jeiHelpers;
		this.ingredientManager = ingredientManager;
		this.ingredientVisibility = ingredientVisibility;
		this.vanillaRecipeFactory = vanillaRecipeFactory;
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
		return vanillaRecipeFactory;
	}

	@Override
	public IIngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
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

		List<IJeiIngredientInfoRecipe> recipes = IngredientInfoRecipe.create(ingredientManager, ingredients, ingredientType, descriptionComponents);
		addRecipes(RecipeTypes.INFORMATION, recipes);
	}
}
