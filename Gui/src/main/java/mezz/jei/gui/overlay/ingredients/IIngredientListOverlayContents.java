package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.InputCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * Ingredient display and input handling for an overlay.
 */
public interface IIngredientListOverlayContents extends IIngredientGridView, IIngredientGridPageNavigation, IRecipeFocusSource {
	/**
	 * Draws the visible ingredients and their navigation controls.
	 */
	void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

	/** Draws ingredient tooltips and drag animations. */
	void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	/**
	 * Highlights where the hovered or dragged ingredient can be dropped.
	 */
	void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	void tick();

	/**
	 * Creates the input handler for deleting the item held by the mouse pointer.
	 */
	IInputTarget createDeleteItemInputHandler();

	/**
	 * Creates the input handler for the ingredient list contents.
	 */
	IInputTarget createInputHandler();

	void registerInputCommands(InputCommands commands, IInternalKeyMappings keys, BooleanSupplier active);

	IInputTarget getResizeInputHandler();

	/**
	 * Tries to start an ingredient drag and returns its interaction, or empty if no drag can start.
	 */
	Optional<IInputInteraction> beginDrag(Screen screen, UserInput input);

	/**
	 * Returns the currently visible ingredients matching the requested type.
	 */
	<T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType);
}
