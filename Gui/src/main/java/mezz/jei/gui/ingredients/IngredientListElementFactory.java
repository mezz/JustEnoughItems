package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientInfo;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static int ingredientAddedIndex = 0;

	private IngredientListElementFactory() {
	}

	public static NonNullList<IListElement<?>> createBaseList(IRegisteredIngredients registeredIngredients) {
		NonNullList<IListElement<?>> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : registeredIngredients.getIngredientTypes()) {
			addToBaseList(ingredientListElements, registeredIngredients, ingredientType);
		}

		return ingredientListElements;
	}

	public static <V> List<IListElement<V>> createList(IRegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType, Collection<V> ingredients) {
		return ingredients.stream()
			.map(i -> registeredIngredients.createTypedIngredient(ingredientType, i))
			.flatMap(Optional::stream)
			.map(IngredientListElementFactory::createOrderedElement)
			.toList();
	}

	public static <V> IListElement<V> createOrderedElement(ITypedIngredient<V> typedIngredient) {
		int orderIndex = ingredientAddedIndex++;
		return new ListElement<>(typedIngredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IListElement<?>> baseList, IRegisteredIngredients registeredIngredients, IIngredientType<V> ingredientType) {
		IIngredientInfo<V> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		Collection<V> ingredients = ingredientInfo.getAllIngredients();
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		ingredients.stream()
			.map(i -> registeredIngredients.createTypedIngredient(ingredientType, i))
			.flatMap(Optional::stream)
			.map(IngredientListElementFactory::createOrderedElement)
			.forEach(baseList::add);
	}

}
