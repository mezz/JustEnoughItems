package mezz.jei.api.ingredients.rendering;

import org.jetbrains.annotations.ApiStatus;

/**
 * Identifies the purpose of an ingredient rendering operation.
 * Each context specifies whether to render decorations, including item counts,
 * durability bars, cooldowns, and registered ingredient decorators.
 *
 * @since 29.54.0
 */
@ApiStatus.NonExtendable
public interface IIngredientRenderContext {
	/**
	 * An ingredient displayed in a list, recipe slot, tooltip, or drag operation.
	 *
	 * @since 29.54.0
	 */
	IIngredientRenderContext INGREDIENT = new IIngredientRenderContext() {
		@Override
		public boolean renderDecorations() {
			return true;
		}
	};

	/**
	 * An ingredient used as a visual icon, such as a category icon or button graphic.
	 *
	 * @since 29.54.0
	 */
	IIngredientRenderContext ICON = new IIngredientRenderContext() {
		@Override
		public boolean renderDecorations() {
			return false;
		}
	};

	/**
	 * Returns whether to render decoration overlays and registered ingredient decorators.
	 *
	 * @since 29.54.0
	 */
	boolean renderDecorations();
}
