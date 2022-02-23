package mezz.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

	private IngredientListElementFactory() {
	}

	public static NonNullList<IIngredientListElement<?>> createBaseList(RegisteredIngredients registeredIngredients) {
		NonNullList<IIngredientListElement<?>> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : registeredIngredients.getIngredientTypes()) {
			addToBaseList(ingredientListElements, registeredIngredients, ingredientType);
		}

		return ingredientListElements;
	}

	public static <V> List<IIngredientListElement<V>> createList(RegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType, Collection<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		return ingredients.stream()
			.map(i -> TypedIngredient.createTyped(registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.map(typedIngredient -> {
				int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
				return (IIngredientListElement<V>) new IngredientListElement<>(typedIngredient, orderIndex);
			})
			.toList();
	}

	public static <V> IIngredientListElement<V> createOrderedElement(RegisteredIngredients registeredIngredients, ITypedIngredient<V> typedIngredient) {
		IIngredientType<V> type = typedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(type);
		int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
		return new IngredientListElement<>(typedIngredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement<?>> baseList, RegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType) {
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		IngredientInfo<V> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		Collection<V> ingredients = ingredientInfo.getAllIngredients();
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		ingredients.stream()
			.map(i -> TypedIngredient.createTyped(registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.forEach(typedIngredient -> {
				int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = new IngredientListElement<>(typedIngredient, orderIndex);
				baseList.add(ingredientListElement);
			});
	}

}
