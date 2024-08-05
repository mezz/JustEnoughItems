package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.Optional;

/**
 * Like {@link IRecipeWidget}, but it also manages {@link IRecipeSlotDrawable}s.
 *
 * These must be created by an {@link ISlottedWidgetFactory}.
 * Pass the factory to {@link IRecipeLayoutBuilder#addSlotToWidget}
 * when creating slots in {@link IRecipeCategory#setRecipe}
 *
 * Once the slots are built, the factory will be called to create your complete {@link ISlottedRecipeWidget}.
 *
 * @since 15.10.0
 */
public interface ISlottedRecipeWidget extends IRecipeWidget {
	/**
	 * @param mouseX the X position of the mouse, relative to its parent element.
	 * @param mouseY the Y position of the mouse, relative to its parent element.
	 *
	 * @return the slot currently under the mouse, if any
	 * @since 15.10.0
	 */
	Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY);
}
