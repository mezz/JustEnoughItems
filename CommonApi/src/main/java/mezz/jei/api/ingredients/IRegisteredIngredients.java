package mezz.jei.api.ingredients;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * Available immediately after ingredients are registered, this is a view of all registered ingredients.
 * This is simpler than {@link IIngredientManager} but it is available much earlier.
 * Get an instance from {@link IJeiHelpers#getRegisteredIngredients()}.
 *
 * @since 11.5.0
 */
public interface IRegisteredIngredients {
    /**
     * Get the type for the given ingredient, if one exists.
     *
     * @since 11.5.0
     */
    <T> Optional<IIngredientType<T>> getIngredientType(T ingredient);

    /**
     * Get a list of all known ingredient types.
     *
     * @since 11.5.0
     */
    @Unmodifiable
    List<IIngredientType<?>> getIngredientTypes();

    <T> IIngredientInfo<T> getIngredientInfo(IIngredientType<T> ingredientType);

    /**
     * Get the ingredient helper for the given ingredient type.
     * This is a convenience function, you can get the same data from {@link #getIngredientInfo(IIngredientType)}.
     *
     * @since 11.5.0
     */
    default <T> IIngredientHelper<T> getIngredientHelper(IIngredientType<T> type) {
        return getIngredientInfo(type)
            .getIngredientHelper();
    }

    /**
     * Get the ingredient renderer for the given ingredient type.
     * This is a convenience function, you can get the same data from {@link #getIngredientInfo(IIngredientType)}.
     *
     * @since 11.5.0
     */
    default <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> type) {
        return getIngredientInfo(type)
            .getIngredientRenderer();
    }

    <T> Optional<ITypedIngredient<T>> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient);
}
