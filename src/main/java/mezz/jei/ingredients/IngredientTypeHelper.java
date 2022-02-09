package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.Focus;
import mezz.jei.render.IngredientListElementRenderer;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public final class IngredientTypeHelper {
	@Nullable
	public static <V> Focus<V> findAndCheckedCast(List<Focus<?>> focuses, IIngredientType<V> ingredientType) {
		for (Focus<?> focus : focuses) {
			Focus<V> vFocus = checkedCast(focus, ingredientType);
			if (vFocus != null) {
				return vFocus;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <V> Focus<V> checkedCast(Focus<?> focus, IIngredientType<V> ingredientType) {
		ITypedIngredient<?> typedValue = focus.getTypedValue();
		if (typedValue.getType() == ingredientType) {
			return (Focus<V>) focus;
		}
		return null;
	}

	public static <T> Optional<IngredientListElementRenderer<T>> checkedCast(@Nullable IngredientListElementRenderer<?> ingredientListElement, IIngredientType<T> ingredientType) {
		if (ingredientListElement == null) {
			return Optional.empty();
		}
		ITypedIngredient<?> value = ingredientListElement.getTypedIngredient();
		if (value.getType() == ingredientType) {
			@SuppressWarnings("unchecked")
			IngredientListElementRenderer<T> castElement = (IngredientListElementRenderer<T>) ingredientListElement;
			return Optional.of(castElement);
		}
		return Optional.empty();
	}
}
