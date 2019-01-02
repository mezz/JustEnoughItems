package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.util.NonNullList;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;

public final class IngredientListElementFactory {
	private static IngredientOrderTracker ORDER_TRACKER = new IngredientOrderTracker();

	private IngredientListElementFactory() {
	}

	public static NonNullList<IIngredientListElement> createBaseList(IIngredientRegistry ingredientRegistry, IModIdHelper modIdHelper) {
		NonNullList<IIngredientListElement> ingredientListElements = NonNullList.create();

		for (IIngredientType<?> ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientRegistry, ingredientType, modIdHelper);
		}

		ingredientListElements.sort(IngredientListElementComparator.INSTANCE);
		return ingredientListElements;
	}

	public static <V> NonNullList<IIngredientListElement<V>> createList(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, Collection<V> ingredients, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

		NonNullList<IIngredientListElement<V>> list = NonNullList.create();
		for (V ingredient : ingredients) {
			if (ingredient != null) {
				int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
				if (ingredientListElement != null) {
					list.add(ingredientListElement);
				}
			}
		}
		return list;
	}

	@Nullable
	public static <V> IIngredientListElement<V> createUnorderedElement(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, V ingredient, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
		return IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, 0);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

		Collection<V> ingredients = ingredientRegistry.getAllIngredients(ingredientType);
		try (ProgressBar progressBar = StartupProgressManager.start("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName(), ingredients.size())) {
			for (V ingredient : ingredients) {
				progressBar.step("");
				if (ingredient != null) {
					int orderIndex = ORDER_TRACKER.getOrderIndex(ingredient, ingredientHelper);
					IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper, orderIndex);
					if (ingredientListElement != null) {
						baseList.add(ingredientListElement);
					}
				}
			}
		}

	}

}
