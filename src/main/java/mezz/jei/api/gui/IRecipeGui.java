package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * An IRecipeGui draws a representation of a single Recipe.
 * It draws tooltips and any necessary animations, but does not handle user navigation.
 */
public interface IRecipeGui {

	/* Set the recipe to display and the ItemStack in focus. */
	void setRecipe(Object recipe, ItemStack focusStack);

	/* Returns true if its recipe is not null. */
	boolean hasRecipe();

	/* Returns the ItemStack at the mouse position, or null if there is none. */
	ItemStack getStackUnderMouse(int mouseX, int mouseY);

	/* Returns the dimensions of the drawn IRecipeGui. Must be constant, regardless of the recipe. */
	int getWidth();
	int getHeight();

	/* Set the position to draw on the screen. */
	void setPosition(int posX, int posY);

	/* Draw the recipe. */
	void draw(Minecraft minecraft, int mouseX, int mouseY);

}
