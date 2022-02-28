package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static int ingredientAddedIndex = 0;

	private IngredientListElementFactory() {
	}

	public static NonNullList<IIngredientListElement<?>> createBaseList(IIngredientManager ingredientManager) {
		NonNullList<IIngredientListElement<?>> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : ingredientManager.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientManager, ingredientType);
		}

		return ingredientListElements;
	}

	public static <V> NonNullList<IIngredientListElement<V>> createList(IIngredientManager ingredientManager, IIngredientType<V> ingredientType, Collection<V> ingredients) {
		NonNullList<IIngredientListElement<V>> list = NonNullList.create();
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				IIngredientListElement<V> ingredientListElement = createOrderedElement(ingredient);
				list.add(ingredientListElement);
			}
		}
		return list;
	}

	public static <V> IIngredientListElement<V> createUnorderedElement(V ingredient) {
		return new IngredientListElement<>(ingredient, 0);
	}

	public static <V> IIngredientListElement<V> createOrderedElement(V ingredient) {
		int orderIndex = ingredientAddedIndex++;
		return new IngredientListElement<>(ingredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement<?>> baseList, IIngredientManager ingredientManager, IIngredientType<V> ingredientType) {
		Collection<V> ingredients = ingredientManager.getAllIngredients(ingredientType);
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				IIngredientListElement<V> ingredientListElement = createOrderedElement(ingredient);
				baseList.add(ingredientListElement);
			}
		}

	}

}
