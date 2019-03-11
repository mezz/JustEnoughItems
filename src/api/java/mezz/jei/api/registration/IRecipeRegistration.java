package mezz.jei.api.registration;

import java.util.Collection;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;

public interface IRecipeRegistration {
	IJeiHelpers getJeiHelpers();

	IIngredientManager getIngredientManager();

	IVanillaRecipeFactory getVanillaRecipeFactory();

	/**
	 * Add the recipes provided by your plugin.
	 */
	void addRecipes(Collection<?> recipes, ResourceLocation recipeCategoryUid);

	/**
	 * Add an info page for an ingredient.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredient      the ingredient to describe
	 * @param ingredientType  the type of the ingredient
	 * @param descriptionKeys Localization keys for info text.
	 *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
	 *                        Long lines are wrapped automatically.
	 *                        Very long entries will span multiple pages automatically.
	 */
	<T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, String... descriptionKeys);

	/**
	 * Add an info page for multiple ingredients together.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredients     the ingredients to describe
	 * @param ingredientType  the type of the ingredients
	 * @param descriptionKeys Localization keys for info text.
	 *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
	 *                        Long lines are wrapped automatically.
	 *                        Very long entries will span multiple pages automatically.
	 */
	<T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys);
}
