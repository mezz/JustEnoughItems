package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;

import java.util.List;
import java.util.Optional;

/**
 * Like {@link IRecipeWidget}, but it also manages {@link IRecipeSlotDrawable}s.
 *
 * Add one to a recipe category by using {@link IRecipeExtrasBuilder#addSlottedWidget(ISlottedRecipeWidget, List)}
 *
 * @since 19.7.0
 */
public interface ISlottedRecipeWidget extends IRecipeWidget {
	/**
	 * @param mouseX the X position of the mouse, relative to its parent element.
	 * @param mouseY the Y position of the mouse, relative to its parent element.
	 *
	 * @return the slot currently under the mouse, if any
	 * @since 19.7.0
	 */
	Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY);
}
