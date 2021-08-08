package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.render.IngredientListElementRenderer;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public final class IngredientTypeHelper {
	@SuppressWarnings("unchecked")
	public static <T> Stream<IIngredientListElement<T>> ofType(Stream<IIngredientListElement<?>> stream, IIngredientType<T> ingredientType) {
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		return (Stream<IIngredientListElement<T>>) (Object) stream.filter(i -> ingredientClass.isInstance(i.getIngredient()));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> IClickedIngredient<T> checkedCast(IClickedIngredient<?> clicked, IIngredientType<T> ingredientType) {
		Object ingredient = clicked.getValue();
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(ingredient)) {
			return (IClickedIngredient<T>) clicked;
		}
		return null;
	}

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

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> IngredientListElementRenderer<T> checkedCast(@Nullable IngredientListElementRenderer<?> ingredientListElement, IIngredientType<T> ingredientType) {
		if (ingredientListElement == null) {
			return null;
		}
		Object ingredient = ingredientListElement.getElement().getIngredient();
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(ingredient)) {
			return (IngredientListElementRenderer<T>) ingredientListElement;
		}
		return null;
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
