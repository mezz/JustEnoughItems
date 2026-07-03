package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.stream.Stream;

/**
 * Renderable ingredient-list contents with paging, input handling, dragging, and visible-ingredient queries.
 */
public interface IIngredientListOverlayContents extends IIngredientGridView, IIngredientGridPageNavigation, IRecipeFocusSource {
	/**
	 * Returns true when there are no visible ingredients in the contents.
	 */
	boolean isEmpty();

	/**
	 * Draws the ingredient-list contents.
	 */
	void draw(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

	/**
	 * Draws tooltips for the ingredient-list contents.
	 */
	void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	/**
	 * Draws foreground elements for the ingredient-list contents.
	 */
	void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	/**
	 * Creates the input handler for the ingredient-list contents.
	 */
	IUserInputHandler createInputHandler();

	/**
	 * Creates the drag handler for the ingredient-list contents.
	 */
	IDragHandler createDragHandler();

	/**
	 * Returns the currently visible ingredients matching the requested type.
	 */
	<T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType);
}
