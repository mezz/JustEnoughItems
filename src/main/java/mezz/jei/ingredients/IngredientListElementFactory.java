package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.ProgressManager;

import javax.annotation.Nullable;
import java.util.Collection;

public final class IngredientListElementFactory {
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
				IngredientListElement<V> ingredientListElement = IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
				if (ingredientListElement != null) {
					list.add(ingredientListElement);
				}
			}
		}
		return list;
	}

	@Nullable
	public static <V> IIngredientListElement<V> createElement(IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, V ingredient, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);
		return IngredientListElement.create(ingredient, ingredientHelper, ingredientRenderer, modIdHelper);
	}

	private static <V> void addToBaseList(NonNullList<IIngredientListElement> baseList, IIngredientRegistry ingredientRegistry, IIngredientType<V> ingredientType, IModIdHelper modIdHelper) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredientType);

		Collection<V> ingredients = ingredientRegistry.getAllIngredients(ingredientType);
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName(), ingredients.size());
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
