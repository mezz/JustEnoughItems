package mezz.jei.api.ingredients;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

public interface IIngredientInfo<T> {
    IIngredientType<T> getIngredientType();

    IIngredientHelper<T> getIngredientHelper();

    IIngredientRenderer<T> getIngredientRenderer();

    @Unmodifiable
    Collection<T> getAllIngredients();

    Optional<T> getIngredientByUid(String uid);
}
