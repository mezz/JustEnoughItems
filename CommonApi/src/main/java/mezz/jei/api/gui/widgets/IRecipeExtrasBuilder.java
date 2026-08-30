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
import org.jetbrains.annotations.ApiStatus;

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
 * <h2>Widget coordinates and ordering</h2>
 * Widget positions are relative to the top-left of the recipe category. Mouse coordinates passed
 * to a widget are relative to that widget's position.
 *
 * The main recipe pass renders the category, slots, widgets, and category decorators in that order.
 * Widgets are rendered in registration order, so a later widget is topmost when widgets overlap.
 *
 * Recipe slots take priority for tooltips. Otherwise, category and decorator tooltip content is
 * added first, followed by widgets in registration order. Widgets are not intended to overlap;
 * if they do, each hovered widget contributes its tooltip in registration order.
 *
 * Widget drawing is not additionally clipped to the category, but tooltip hit-testing is: the
 * mouse must be within both the widget and the category.
 *
 * @see IRecipeWidget
 * @see IJeiInputHandler
 * @see IJeiGuiEventListener
 *
 * @since 19.6.0
 */
@ApiStatus.NonExtendable
public interface IRecipeExtrasBuilder {

	/**
	 * Get the recipe slots that were created in {@link IRecipeCategory#setRecipe}.
	 *
	 * @since 19.19.3
	 */
	IRecipeSlotDrawablesView getRecipeSlots();

	/**
	 * Add a {@link IDrawable} for the recipe category at the given position.
	 *
	 * @since 19.19.0
	 * @deprecated use {@link #addDrawableWidget(IDrawable)}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	void addDrawable(IDrawable drawable, int xPos, int yPos);

	/**
	 * Add a {@link IDrawable} for the recipe category, and place it after with {@link IPlaceable} methods.
	 *
	 * @since 19.19.1
	 * @deprecated use {@link #addDrawableWidget(IDrawable)}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	IPlaceable<?> addDrawable(IDrawable drawable);

	/**
	 * Add a drawable widget for the recipe category.
	 *
	 * Its tooltip bounds are derived from {@link IDrawable#getWidth()} and
	 * {@link IDrawable#getHeight()}, including drawable-builder padding. Configure it with methods
	 * such as {@link IDrawableWidget#setPosition(int, int)} and
	 * {@link IDrawableWidget#setTooltip(FormattedText)}.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addDrawableWidget(IDrawable drawable);

	/**
	 * Add a hover-only rectangular tooltip region, then configure its tooltip with
	 * {@link IDrawableWidget#setTooltip(FormattedText)} or another {@code setTooltip} overload.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addTooltipArea(int xPos, int yPos, int width, int height);

	/**
	 * Add a {@link IRecipeWidget} for the recipe category.
	 *
	 * @since 19.7.0
	 */
	void addWidget(IRecipeWidget widget);

	/**
	 * Add a {@link ISlottedRecipeWidget} for the recipe category, and
	 * mark that the slots are going to be handled by the slotted widget.
	 *
	 * @since 19.19.3
	 */
	void addSlottedWidget(ISlottedRecipeWidget widget, List<IRecipeSlotDrawable> slots);

	/**
	 * Add a {@link IJeiInputHandler} for the recipe category.
	 *
	 * @since 19.6.0
	 */
	void addInputHandler(IJeiInputHandler inputHandler);

	/**
	 * Add a {@link GuiEventListener} for the recipe category.
	 *
	 * @since 19.6.0
	 */
	void addGuiEventListener(IJeiGuiEventListener guiEventListener);

	/**
	 * Create and add a new scroll box widget.
	 * Handles displaying drawable contents in a scrolling area with a scrollbar.
	 *
	 * Set the contents by using the methods in {@link IScrollBoxWidget}.
	 *
	 * @since 19.18.9
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
	 * @since 19.19.3
	 */
	IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows);

	/**
	 * Add a vanilla-style recipe arrow to the recipe layout.
	 *
	 * @since 19.19.1
	 * @deprecated use {@link #addRecipeArrowWidget()}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	IPlaceable<?> addRecipeArrow();

	/**
	 * Add a vanilla-style recipe arrow widget to the recipe layout.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addRecipeArrowWidget();

	/**
	 * Add a vanilla-style recipe plus sign to the recipe layout.
	 *
	 * @since 19.19.1
	 * @deprecated use {@link #addRecipePlusSignWidget()}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	IPlaceable<?> addRecipePlusSign();

	/**
	 * Add a vanilla-style recipe plus-sign widget to the recipe layout.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addRecipePlusSignWidget();

	/**
	 * Add a vanilla-style recipe arrow that fills over time in a loop.
	 *
	 * @since 19.19.1
	 * @deprecated use {@link #addAnimatedRecipeArrowWidget(int)}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	IPlaceable<?> addAnimatedRecipeArrow(int ticksPerCycle);

	/**
	 * Add a vanilla-style animated recipe-arrow widget to the recipe layout.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addAnimatedRecipeArrowWidget(int ticksPerCycle);

	/**
	 * Add a vanilla-style recipe flame that empties over time in a loop.
	 *
	 * @since 19.19.1
	 * @deprecated use {@link #addAnimatedRecipeFlameWidget(int)}
	 */
	@Deprecated(since = "29.34.0", forRemoval = true)
	IPlaceable<?> addAnimatedRecipeFlame(int cookTime);

	/**
	 * Add a vanilla-style animated recipe-flame widget to the recipe layout.
	 *
	 * @since 29.34.0
	 */
	IDrawableWidget addAnimatedRecipeFlameWidget(int cookTime);

	/**
	 * Add text to the recipe layout.
	 *
	 * Automatically supports text wrapping and truncation of very long lines.
	 * If text is truncated, it is displayed with an ellipsis and its full text is shown first in the
	 * tooltip.
	 *
	 * Text can be vertically and horizontally aligned using the methods in {@link ITextWidget}.
	 * By default, text is vertically aligned "top" and horizontally aligned "left" inside the area given.
	 *
	 * @since 19.19.1
	 */
	default ITextWidget addText(FormattedText text, int maxWidth, int maxHeight) {
		return addText(List.of(text), maxWidth, maxHeight);
	}

	/**
	 * Add text to the recipe layout.
	 *
	 * Automatically supports text wrapping and truncation of very long lines.
	 * If text is truncated, it is displayed with an ellipsis and its full text is shown first in the
	 * tooltip.
	 *
	 * Text can be vertically and horizontally aligned using the methods in {@link ITextWidget}.
	 * By default, text is vertically aligned "top" and horizontally aligned "left" inside the area given.
	 *
	 * @since 19.19.1
	 */
	ITextWidget addText(List<FormattedText> text, int maxWidth, int maxHeight);
}
