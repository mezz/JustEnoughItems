package mezz.jei.api.ingredients;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * @since 11.5.0
 */
public interface IRegisteredIngredients {
    <T> IIngredientHelper<T> getIngredientHelper(IIngredientType<T> type);

    <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> type);

    @Unmodifiable
    List<IIngredientType<?>> getIngredientTypes();

    <T> IIngredientType<T> getIngredientType(T ingredient);

    <T> IIngredientInfo<T> getIngredientInfo(IIngredientType<T> ingredientType);

    <T> Optional<ITypedIngredient<T>> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient);
}
