package mezz.jei.api.gui;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;

/**
 * Represents the layout of one recipe on-screen.
 * It is passed to plugins in {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}.
 *
 * @see IRecipeLayoutDrawable
 */
public interface IRecipeLayout {
	/**
	 * Contains all the itemStacks displayed on this recipe layout.
	 * Init and set them in your recipe category.
	 */
	IGuiItemStackGroup getItemStacks();

	/**
	 * Contains all the fluidStacks displayed on this recipe layout.
	 * Init and set them in your recipe category.
	 */
	IGuiFluidStackGroup getFluidStacks();

	/**
	 * Get all the ingredients of one type that are displayed on this recipe layout.
	 * Init and set them in your recipe category.
	 *
	 * This method is for handling custom item types, registered with {@link IModIngredientRegistration}.
	 *
	 * @see #getItemStacks()
	 * @see #getFluidStacks()
	 */
	<T> IGuiIngredientGroup<T> getIngredientsGroup(IIngredientType<T> ingredientType);

	/**
	 * The current search focus. Set by the player when they look up the recipe.
	 * The ingredient being looked up is the focus.
	 * Returns null if there is no focus, or if the focus is a different type
	 * @since JEI 7.0.1
	 */
	@Nullable
	<V> IFocus<V> getFocus(IIngredientType<V> ingredientType);

	/**
	 * Moves the recipe transfer button's position relative to the recipe layout.
	 * By default the recipe transfer button is at the bottom, to the right of the recipe.
	 * If it doesn't fit there, you can use this to move it when you init the recipe layout.
	 */
	void moveRecipeTransferButton(int posX, int posY);

	/**
	 * Adds a shapeless icon to the top right of the recipe, that shows a tooltip saying "shapeless" when hovered over.
	 */
	void setShapeless();

}
