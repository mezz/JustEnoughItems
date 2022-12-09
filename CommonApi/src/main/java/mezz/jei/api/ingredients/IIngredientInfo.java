package mezz.jei.api.ingredients;

import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * Stores all known information about a single ingredient type.
 * Get an instance from {@link IRegisteredIngredients#getIngredientInfo(IIngredientType)}.
 *
 * @since 11.5.0
 */
public interface IIngredientInfo<T> {
    /**
     * Get the ingredient type that this instance knows about.
     *
     * @since 11.5.0
     */
    IIngredientType<T> getIngredientType();

    /**
     * Get the ingredient helper for the ingredient type.
     *
     * @since 11.5.0
     */
    IIngredientHelper<T> getIngredientHelper();

    /**
     * Get the ingredient renderer for the ingredient type.
     *
     * @since 11.5.0
     */
    IIngredientRenderer<T> getIngredientRenderer();

    /**
     * Get a list of all ingredients for the ingredient type.
     *
     * @since 11.5.0
     */
    @Unmodifiable
    Collection<T> getAllIngredients();

    /**
     * Get an ingredient by the given unique id.
     * This uses the uids from {@link IIngredientHelper#getUniqueId(Object, UidContext)}
     *
     * @since 11.5.0
     */
    Optional<T> getIngredientByUid(String uid);
}
