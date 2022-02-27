package mezz.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.gui.ingredients.IListElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

	private IngredientListElementFactory() {
	}

	public static NonNullList<IListElement<?>> createBaseList(RegisteredIngredients registeredIngredients) {
		NonNullList<IListElement<?>> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : registeredIngredients.getIngredientTypes()) {
			addToBaseList(ingredientListElements, registeredIngredients, ingredientType);
		}

		return ingredientListElements;
	}

	public static <V> List<IListElement<V>> createList(RegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType, Collection<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		return ingredients.stream()
			.map(i -> TypedIngredient.createTyped(registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.map(typedIngredient -> {
				int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
				return (IListElement<V>) new ListElement<>(typedIngredient, orderIndex);
			})
			.toList();
	}

	public static <V> IListElement<V> createOrderedElement(RegisteredIngredients registeredIngredients, ITypedIngredient<V> typedIngredient) {
		IIngredientType<V> type = typedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(type);
		int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
		return new ListElement<>(typedIngredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IListElement<?>> baseList, RegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType) {
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		IngredientInfo<V> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		Collection<V> ingredients = ingredientInfo.getAllIngredients();
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		ingredients.stream()
			.map(i -> TypedIngredient.createTyped(registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.forEach(typedIngredient -> {
				int orderIndex = ORDER_TRACKER.getOrderIndex(typedIngredient, ingredientHelper);
				ListElement<V> ingredientListElement = new ListElement<>(typedIngredient, orderIndex);
				baseList.add(ingredientListElement);
			});
	}

}
