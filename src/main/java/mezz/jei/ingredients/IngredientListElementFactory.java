package mezz.jei.ingredients;

import java.util.Collection;

import net.minecraft.core.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IIngredientListElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

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
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		NonNullList<IIngredientListElement<V>> list = NonNullList.create();
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = new IngredientListElement<>(ingredient, orderIndex);
				list.add(ingredientListElement);
			}
		}
		return list;
	}

	public static <V> IIngredientListElement<V> createUnorderedElement(V ingredient) {
		return new IngredientListElement<>(ingredient, 0);
	}

	public static <V> IIngredientListElement<V> createOrderedElement(IIngredientManager ingredientManager, IIngredientType<V> ingredientType, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
		return new IngredientListElement<>(ingredient, orderIndex);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement<?>> baseList, IIngredientManager ingredientManager, IIngredientType<V> ingredientType) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		Collection<V> ingredients = ingredientManager.getAllIngredients(ingredientType);
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = new IngredientListElement<>(ingredient, orderIndex);
				baseList.add(ingredientListElement);
			}
		}

	}

}
