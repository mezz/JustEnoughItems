package mezz.jei.api.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * An extension of {@link IRecipeLayout} for addons that want to draw the layouts themselves somewhere.
 * <p>
 * Create an instance with {@link IRecipeRegistry#createRecipeLayoutDrawable(IRecipeCategory, IRecipeWrapper, IFocus)}
 */
public interface IRecipeLayoutDrawable extends IRecipeLayout {
	/**
	 * Set the position of the recipe layout in screen coordinates.
	 * To help decide on the position, you can get the width and height of this recipe from {@link IRecipeCategory#getBackground()}.
	 */
	void setPosition(int posX, int posY);

	/**
	 * Draw the recipe without overlays such as item tool tips.
	 *
	 * @since JEI 4.7.4
	 */
	void drawRecipe(Minecraft minecraft, int mouseX, int mouseY);

	/**
	 * Draw the recipe overlays such as item tool tips.
	 *
	 * @since JEI 4.7.4
	 */
	void drawOverlays(Minecraft minecraft, int mouseX, int mouseY);

	/**
	 * Returns true if the mouse is hovering over the recipe.
	 * Hovered recipes should be drawn after other recipes to have the drawn tooltips overlap other elements properly.
	 */
	boolean isMouseOver(int mouseX, int mouseY);

	/**
	 * Returns the ingredient currently under the mouse, if there is one.
	 * Can be an ItemStack, FluidStack, or any other ingredient registered with JEI.
	 */
	@Nullable
	Object getIngredientUnderMouse(int mouseX, int mouseY);

	// DEPRECATED BELOW

	/**
	 * Draw the recipe layout.
	 *
	 * @deprecated since JEI 4.7.4, use {@link #drawRecipe(Minecraft, int, int)} and {@link #drawOverlays(Minecraft, int, int)}
	 */
	@Deprecated
	void draw(Minecraft minecraft, int mouseX, int mouseY);
}
