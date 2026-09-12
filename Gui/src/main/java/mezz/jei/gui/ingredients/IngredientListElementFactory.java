package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientListElementFactory() {
	}

	public static List<IListElementInfo<?>> createBaseList(IIngredientManager ingredientManager, IIngredientFilterConfig config, IModIdHelper modIdHelper) {
		List<IListElementInfo<?>> ingredientListElements = new ArrayList<>();

		for (IIngredientType<?> ingredientType : ingredientManager.getRegisteredIngredientTypes()) {
			addToBaseList(ingredientListElements, ingredientManager, ingredientType, config, modIdHelper);
		}

		return ingredientListElements;
	}

	public static <V> List<IListElementInfo<V>> createTestList(
		IIngredientManager ingredientManager,
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		IIngredientFilterConfig config,
		IModIdHelper modIdHelper
	) {
		return ingredients.stream()
			.map(i -> ingredientManager.createTypedIngredient(ingredientType, i, false))
			.flatMap(Optional::stream)
			.map(i -> ListElementInfo.create(i, ingredientManager, config, modIdHelper))
			.filter(Objects::nonNull)
			.toList();
	}

	public static List<IListElementInfo<?>> rebuildList(
		IIngredientManager ingredientManager,
		Collection<IListElement<?>> elements,
		IIngredientFilterConfig config,
		IModIdHelper modIdHelper
	) {
		List<IListElementInfo<?>> results = new ArrayList<>();

		for (IListElement<?> element : elements) {
			IListElementInfo<?> orderedElement = ListElementInfo.createFromElement(element, ingredientManager, config, modIdHelper);
			if (orderedElement != null) {
				results.add(orderedElement);
			}
		}

		return results;
	}

	private static <V> void addToBaseList(
		List<IListElementInfo<?>> baseList,
		IIngredientManager ingredientManager,
		IIngredientType<V> ingredientType,
		IIngredientFilterConfig config,
		IModIdHelper modIdHelper
	) {
		Collection<ITypedIngredient<V>> typedIngredients = ingredientManager.getAllTypedIngredients(ingredientType);
		LOGGER.debug("Registering ingredients: {}", ingredientType.getIngredientClass().getSimpleName());
		for (ITypedIngredient<V> typedIngredient : typedIngredients) {
			IListElementInfo<V> orderedElement = ListElementInfo.create(typedIngredient, ingredientManager, config, modIdHelper);
			if (orderedElement != null) {
				baseList.add(orderedElement);
			}
		}
	}

}
