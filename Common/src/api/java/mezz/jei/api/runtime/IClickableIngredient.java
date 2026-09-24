package mezz.jei.api.runtime;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;

/**
 * A clickable ingredient drawn on the screen, that JEI can use for recipe lookups.
 *
 * This can be an ingredient drawn in a GUI container slot, a fluid tank,
 * or anything else that holds ingredients.
 *
 * Create one with {@link IIngredientManager#getClickableIngredientFactory()}
 * @see IClickableIngredientFactory
 *
 * @since 11.5.0
 */
public interface IClickableIngredient<T> {
	/**
	 * Get the typed ingredient that can be looked up by JEI for recipes.
	 *
	 * @since 11.5.0
	 */
	ITypedIngredient<T> getTypedIngredient();

	/**
	 * @since 19.12.0
	 *
	 * @deprecated use {@link #getTypedIngredient()}
	 */
	@Deprecated(since = "21.2.0", forRemoval = true)
	default IIngredientType<T> getIngredientType() {
		return getTypedIngredient().getType();
	}

	/**
	 * @since 19.12.0
	 *
	 * @deprecated use {@link #getTypedIngredient()}
	 */
	@Deprecated(since = "21.2.0", forRemoval = true)
	default T getIngredient() {
		ITypedIngredient<T> typedIngredient = getTypedIngredient();
		return typedIngredient.getIngredient();
	}

	/**
	 * Get the area that this clickable ingredient is drawn in, in absolute screen coordinates.
	 * This is used for click handling, to ensure the mouse-down and mouse-up are on the same ingredient.
	 *
	 * @since 11.5.0
	 */
	Rect2i getArea();
}
