package mezz.jei.config.sorting;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.config.sorting.serializers.SortingSerializers;
import mezz.jei.ingredients.IListElementInfo;

import java.io.File;
import java.util.Comparator;

public class IngredientTypeSortingConfig extends MappedSortingConfig<IListElementInfo<?>, String> {
	public IngredientTypeSortingConfig(File file) {
		super(file, SortingSerializers.STRING, IngredientTypeSortingConfig::getIngredientTypeString);
	}

	public static String getIngredientTypeString(IListElementInfo<?> info) {
		ITypedIngredient<?> typedIngredient = info.getTypedIngredient();
		return getIngredientTypeString(typedIngredient.getType());
	}

	public static String getIngredientTypeString(IIngredientType<?> ingredientType) {
		return ingredientType.getIngredientClass().getName();
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		String itemStackIngredientType = getIngredientTypeString(VanillaTypes.ITEM);
		Comparator<String> itemStackFirst = Comparator.comparing((String s) -> s.equals(itemStackIngredientType)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return itemStackFirst.thenComparing(naturalOrder);
	}

}
