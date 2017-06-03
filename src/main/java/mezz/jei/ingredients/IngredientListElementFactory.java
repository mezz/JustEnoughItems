package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import net.minecraftforge.fml.common.ProgressManager;

public final class IngredientListElementFactory {
	private IngredientListElementFactory() {
	}

	public static List<IIngredientListElement> createBaseList(IIngredientRegistry ingredientRegistry, IModIdHelper modIdHelper) {
		List<IIngredientListElement> ingredientListElements = new LinkedList<IIngredientListElement>();

		for (Class ingredientClass : ingredientRegistry.getRegisteredIngredientClasses()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientClass, modIdHelper);
		}

		Collections.sort(ingredientListElements, IngredientListElementComparator.INSTANCE);
		return ingredientListElements;
	}

	public static <V> List<IIngredientListElement> createList(IIngredientRegistry ingredientRegistry, Class<V> ingredientClass, List<V> ingredients, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientClass);

		List<IIngredientListElement> list = new ArrayList<IIngredientListElement>();
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
				if (ingredientListElement != null) {
					list.add(ingredientListElement);
				}
			}
		}
		return list;
	}

	private static <V> void addToBaseList(List<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, Class<V> ingredientClass, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientClass);

		List<V> ingredients = ingredientRegistry.getIngredients(ingredientClass);
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients: " + ingredientClass.getSimpleName(), ingredients.size());
		for (V ingredient : ingredients) {
			progressBar.step("");
			if (ingredient != null) {
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
				if (ingredientListElement != null) {
					baseList.add(ingredientListElement);
				}
			}
		}
		ProgressManager.pop(progressBar);
	}

}
