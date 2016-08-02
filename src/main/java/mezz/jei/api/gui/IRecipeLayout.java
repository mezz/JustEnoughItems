package mezz.jei.api.gui;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * Represents the layout of one recipe on-screen.
 * Plugins interpret a recipe wrapper to set the properties here.
 * It is passed to plugins in {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper)}.
 */
public interface IRecipeLayout {
	/**
	 * Contains all the itemStacks displayed on this recipe layout.
	 * Init and set them in your recipe category.
	 */
	@Nonnull
	IGuiItemStackGroup getItemStacks();

	/**
	 * Contains all the fluidStacks displayed on this recipe layout.
	 * Init and set them in your recipe category.
	 */
	@Nonnull
	IGuiFluidStackGroup getFluidStacks();

	/**
	 * Moves the recipe transfer button's position relative to the recipe layout.
	 * By default the recipe transfer button is at the bottom, to the right of the recipe.
	 * If it doesn't fit there, you can use this to move it when you init the recipe layout.
	 */
	void setRecipeTransferButton(int posX, int posY);
}
