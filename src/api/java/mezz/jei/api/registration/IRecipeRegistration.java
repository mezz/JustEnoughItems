package mezz.jei.api.registration;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientVisibility;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;

public interface IRecipeRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * The {@link IVanillaRecipeFactory} allows creation of vanilla recipes.
	 */
	IVanillaRecipeFactory getVanillaRecipeFactory();

	/**
	 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
	 * ingredients based on what is visible in JEI.
	 *
	 * @since 9.3.1
	 */
	IIngredientVisibility getIngredientVisibility();

	/**
	 * Add the recipes provided by your plugin.
	 *
	 * @deprecated use {@link #addRecipes(RecipeType, List)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	void addRecipes(Collection<?> recipes, ResourceLocation recipeCategoryUid);

	/**
	 * Add the recipes provided by your plugin.
	 *
	 * @since 9.5.0
	 */
	<T> void addRecipes(RecipeType<T> recipeType, List<T> recipes);

	/**
	 * Add an info page for an ingredient.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredient            The ingredient to describe
	 * @param ingredientType        The type of the ingredient
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 7.6.4
	 */
	<T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, Component... descriptionComponents);

	/**
	 * Add an info page for multiple ingredients together.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredients           The ingredients to describe
	 * @param ingredientType        The type of the ingredients
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 7.6.4
	 */
	<T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents);
}
