package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * A smooth-scrolling area with a scrollbar.
 *
 * Create one with {@link IGuiHelper#createScrollBoxWidget}, and then
 * add it to your recipe in {@link IRecipeCategory#createRecipeExtras}
 * using {@link IRecipeExtrasBuilder#addWidget} and {@link IRecipeExtrasBuilder#addInputHandler}.
 *
 * @since 19.8.0
 */
public interface IScrollBoxWidget extends IRecipeWidget, IJeiInputHandler {

}
