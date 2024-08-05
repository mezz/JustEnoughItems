package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Create per-recipe extras like {@link IJeiInputHandler}.
 *
 * These have access to a specific recipe, and will persist as long as a recipe layout is on screen,
 * so they can be used for caching and displaying recipe-specific
 * information more easily than from the recipe category directly.
 *
 * An instance of this is given to your {@link IRecipeCategory#createRecipeExtras} method.
 *
 * @since 15.9.0
 */
public interface IRecipeExtrasBuilder {
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
}
