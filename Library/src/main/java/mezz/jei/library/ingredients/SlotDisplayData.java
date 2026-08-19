package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.List;
import java.util.Objects;

public record SlotDisplayData<T>(
	List<ITypedIngredient<T>> ingredients,
	SlotDisplayInfo info
) {
	public SlotDisplayData {
		ingredients = List.copyOf(ingredients);
		Objects.requireNonNull(info);
	}
}
