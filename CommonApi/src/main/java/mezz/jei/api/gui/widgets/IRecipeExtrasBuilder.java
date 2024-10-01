package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

/**
 * Create per-recipe extras like {@link IRecipeWidget} and {@link IJeiInputHandler}.
 *
 * These have access to a specific recipe, and will persist as long as a recipe layout is on screen,
 * so they can be used for caching and displaying recipe-specific
 * information more easily than from the recipe category directly.
 *
 * An instance of this is given to your {@link IRecipeCategory#createRecipeExtras} method.
 *
 * @see IRecipeWidget
 * @see IJeiInputHandler
 * @see IJeiGuiEventListener
 *
 * @since 15.9.0
 */
public interface IRecipeExtrasBuilder {

	/**
	 * Get the recipe slots that were created in {@link IRecipeCategory#setRecipe}.
	 *
	 * @since 15.20.0
	 */
	IRecipeSlotDrawablesView getRecipeSlots();

	/**
	 * Add a {@link IDrawable} for the recipe category at the given position.
	 *
	 * @since 15.20.0
	 */
	void addDrawable(IDrawable drawable, int xPos, int yPos);

	/**
	 * Add a {@link IDrawable} for the recipe category, and place it after with {@link IPlaceable} methods.
	 *
	 * @since 15.20.0
	 */
	IPlaceable<?> addDrawable(IDrawable drawable);

	/**
	 * Add a {@link IRecipeWidget} for the recipe category.
	 *
	 * @since 15.10.0
	 */
	void addWidget(IRecipeWidget widget);

	/**
	 * Add a {@link ISlottedRecipeWidget} for the recipe category, and
	 * mark that the slots are going to be handled by the slotted widget.
	 *
	 * @since 15.20.0
	 */
	void addSlottedWidget(ISlottedRecipeWidget widget, List<IRecipeSlotDrawable> slots);

	/**
	 * Add a {@link IJeiInputHandler} for the recipe category.
	 *
	 * @since 15.9.0
	 */
	void addInputHandler(IJeiInputHandler inputHandler);

	/**
	 * Add a {@link GuiEventListener} for the recipe category.
	 *
	 * @since 15.9.0
	 */
	void addGuiEventListener(IJeiGuiEventListener guiEventListener);

	/**
	 * Create and add a new scroll box widget.
	 * Handles displaying drawable contents in a scrolling area with a scrollbar.
	 *
	 * Set the contents by using the methods in {@link IScrollBoxWidget}.
	 *
	 * @since 15.20.0
	 */
	IScrollBoxWidget addScrollBoxWidget(int width, int height, int xPos, int yPos);

	/**
	 * Create and add a new scroll grid widget.
	 * Handles displaying ingredients in a scrolling area with a scrollbar, similar to the vanilla creative menu.
	 *
	 * Get slots for this from {@link #getRecipeSlots()}.
	 *
	 * You can move the resulting grid by using the {@link IScrollGridWidget}'s {@link IPlaceable} methods.
	 *
	 * @since 15.20.0
	 */
	IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows);

	/**
	 * Add a vanilla-style recipe arrow to the recipe layout.
	 *
	 * @since 15.20.0
	 */
	IPlaceable<?> addRecipeArrow();

	/**
	 * Add a vanilla-style recipe plus sign to the recipe layout.
	 *
	 * @since 15.20.0
	 */
	IPlaceable<?> addRecipePlusSign();

	/**
	 * Add a vanilla-style recipe arrow that fills over time in a loop.
	 *
	 * @since 15.20.0
	 */
	IPlaceable<?> addAnimatedRecipeArrow(int ticksPerCycle);

	/**
	 * Add a vanilla-style recipe flame that empties over time in a loop.
	 *
	 * @since 15.20.0
	 */
	IPlaceable<?> addAnimatedRecipeFlame(int cookTime);

	/**
	 * Add text to the recipe layout.
	 *
	 * Automatically supports text wrapping and truncation of very long lines.
	 * If text is truncated, it will be displayed with an ellipsis (...) and can be viewed fully with a tooltip.
	 *
	 * Text can be vertically and horizontally aligned using the methods in {@link ITextWidget}.
	 * By default, text is vertically aligned "top" and horizontally aligned "left" inside the area given.
	 *
	 * @since 15.20.0
	 */
	default ITextWidget addText(FormattedText text, int maxWidth, int maxHeight) {
		return addText(List.of(text), maxWidth, maxHeight);
	}

	/**
	 * Add text to the recipe layout.
	 *
	 * Automatically supports text wrapping and truncation of very long lines.
	 * If text is truncated, it will be displayed with an ellipsis (...) and can be viewed fully with a tooltip.
	 *
	 * Text can be vertically and horizontally aligned using the methods in {@link ITextWidget}.
	 * By default, text is vertically aligned "top" and horizontally aligned "left" inside the area given.
	 *
	 * @since 15.20.0
	 */
	ITextWidget addText(List<FormattedText> text, int maxWidth, int maxHeight);
}
