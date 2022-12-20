package mezz.jei.common.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutableRect2i;

public interface IClickableIngredientInternal<T> {
    ITypedIngredient<T> getTypedIngredient();

    ImmutableRect2i getArea();

    /**
     * Returns true if this clickable slot allows players to cheat ingredients from it
     * (when the server has granted them permission to cheat).
     *
     * This is generally only true in the JEI ingredient list and bookmark list.
     */
    boolean allowsCheating();

    /**
     * Most GUIs shouldn't allow JEI to click to set the focus,
     * because it would conflict with their normal behavior.
     *
     * JEI's recipe GUI has clickable slots that do allow click to focus,
     * in order to let players navigate recipes.
     */
    boolean canClickToFocus();
}
