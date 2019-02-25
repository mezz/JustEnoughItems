package mezz.jei.ingredients;

import java.util.Collection;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.util.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientManager;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;

public final class IngredientListElementFactory {
	private static IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

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
		try (ProgressBar progressBar = StartupProgressManager.start("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName(), ingredients.size())) {
			for (V ingredient : ingredients) {
				progressBar.step("");
				if (ingredient != null) {
					int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
					IngredientListElement<V> ingredientListElement = new IngredientListElement<>(ingredient, orderIndex);
					baseList.add(ingredientListElement);
				}
			}
		}

	}

}
