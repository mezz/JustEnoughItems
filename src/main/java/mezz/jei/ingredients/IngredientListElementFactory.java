package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.ingredients.IListElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class IngredientListElementFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private static int ingredientAddedIndex = 0;

	private IngredientListElementFactory() {
	}

	public static List<IListElement<?>> createBaseList(List<RawIngredientInfo<?>> infos) {
		return infos.stream()
			.flatMap(IngredientListElementFactory::createOrderedElementStream)
			.toList();
	}

	public static <V> IListElement<V> createOrderedElement(ITypedIngredient<V> typedIngredient) {
		int orderIndex = ingredientAddedIndex++;
		return new ListElement<>(typedIngredient, orderIndex);
	}

	private static <V> Stream<IListElement<?>> createOrderedElementStream(RawIngredientInfo<V> info) {
		Collection<V> ingredients = info.getAllIngredients();
		IIngredientType<V> ingredientType = info.getIngredientType();
		LOGGER.debug("Registering ingredients: " + ingredientType.getIngredientClass().getSimpleName());
		return ingredients.stream()
			.map(i -> TypedIngredient.createTypedUnchecked(ingredientType, i))
			.flatMap(Optional::stream)
			.map(IngredientListElementFactory::createOrderedElement);
	}

}
