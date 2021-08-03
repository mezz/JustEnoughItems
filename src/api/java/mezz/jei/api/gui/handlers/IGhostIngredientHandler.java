package mezz.jei.api.gui.handlers;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

/**
 * Lets mods accept ghost ingredients from JEI.
 * These ingredients are dragged from the ingredient list on to your gui, and are useful
 * for setting recipes or anything else that does not need the real ingredient to exist.
 */
public interface IGhostIngredientHandler<T extends Screen> {
	/**
	 * Called when a player wants to drag an ingredient on to your gui.
	 * Return the targets that can accept the ingredient.
	 *
	 * This is called when a player hovers over an ingredient with doStart=false,
	 * and called again when they pick up the ingredient with doStart=true.
	 */
	<I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart);

	/**
	 * Called when the player is done dragging an ingredient.
	 * If the drag succeeded, {@link Target#accept(Object)} was called before this.
	 * Otherwise, the player failed to drag an ingredient to a {@link Target}.
	 */
	void onComplete();

	/**
	 * @return true if JEI should highlight the targets for the player.
	 * false to handle highlighting yourself.
	 */
	default boolean shouldHighlightTargets() {
		return true;
	}

	interface Target<I> extends Consumer<I> {
		/**
		 * @return the area (in screen coordinates) where the ingredient can be dropped.
		 */
		Rect2i getArea();

		/**
		 * Called with the ingredient when it is dropped on the target.
		 */
		@Override
		void accept(I ingredient);
	}
}
