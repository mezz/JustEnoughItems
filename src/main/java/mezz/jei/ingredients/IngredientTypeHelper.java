package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.render.IngredientListElementRenderer;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public final class IngredientTypeHelper {
	@SuppressWarnings("unchecked")
	@Nullable
	public static <V> Focus<V> checkedCast(@Nullable Focus<?> focus, IIngredientType<V> ingredientType) {
		if (focus == null) {
			return null;
		}
		Class<? extends V> ingredientClass = ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(focus.getValue())) {
			return (Focus<V>) focus;
		}
		return null;
	}

	public static <T> Optional<IngredientListElementRenderer<T>> checkedCast(@Nullable IngredientListElementRenderer<?> ingredientListElement, IIngredientType<T> ingredientType) {
		if (ingredientListElement == null) {
			return Optional.empty();
		}
		Object ingredient = ingredientListElement.getIngredient();
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(ingredient)) {
			@SuppressWarnings("unchecked")
			IngredientListElementRenderer<T> castElement = (IngredientListElementRenderer<T>) ingredientListElement;
			return Optional.of(castElement);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> GuiIngredient<T> checkedCast(GuiIngredient<?> guiIngredient, IIngredientType<T> ingredientType) {
		if (guiIngredient.getIngredientType() == ingredientType) {
			return (GuiIngredient<T>) guiIngredient;
		}
		return null;
	}
}
