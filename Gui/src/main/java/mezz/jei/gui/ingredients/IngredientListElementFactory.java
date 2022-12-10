package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
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

	public static NonNullList<IListElement<?>> createBaseList(IIngredientManager ingredientManager) {
		NonNullList<IListElement<?>> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : ingredientManager.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientManager, ingredientType);
		}

		return ingredientListElements;
	}

	public static <V> List<IListElement<V>> createList(IIngredientManager ingredientManager, IIngredientType<V> ingredientType, Collection<V> ingredients) {
		return ingredients.stream()
			.map(i -> ingredientManager.createTypedIngredient(ingredientType, i))
			.flatMap(Optional::stream)
			.map(IngredientListElementFactory::createOrderedElement)
			.toList();
	}

	public static <V> IListElement<V> createOrderedElement(ITypedIngredient<V> typedIngredient) {
		int orderIndex = ingredientAddedIndex++;
		return new ListElement<>(typedIngredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IListElement<?>> baseList, IIngredientManager ingredientManager, IIngredientType<V> ingredientType) {
		Collection<V> ingredients = ingredientManager.getAllIngredients(ingredientType);
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		ingredients.stream()
			.map(i -> ingredientManager.createTypedIngredient(ingredientType, i))
			.flatMap(Optional::stream)
			.map(IngredientListElementFactory::createOrderedElement)
			.forEach(baseList::add);
	}

}
