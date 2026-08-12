package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record SlotIngredient<T>(
	ITypedIngredient<T> typedIngredient,
	@Nullable SlotDisplayData<T> slotDisplayData
) {
	public SlotIngredient {
		Objects.requireNonNull(typedIngredient);
	}

	public SlotIngredient(ITypedIngredient<T> typedIngredient) {
		this(typedIngredient, null);
	}
}
