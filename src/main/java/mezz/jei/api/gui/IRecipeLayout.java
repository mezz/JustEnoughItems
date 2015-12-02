package mezz.jei.api.gui;

import javax.annotation.Nonnull;

public interface IRecipeLayout {
	@Nonnull
	IGuiItemStackGroup getItemStacks();

	@Nonnull
	IGuiFluidStackGroup getFluidStacks();

	/**
	 * Enables the recipe transfer button and sets its position relative to the recipe layout.
	 * The button transfers items in the user's inventory into the crafting area to set a recipe.
	 */
	void setRecipeTransferButton(int posX, int posY);
}
