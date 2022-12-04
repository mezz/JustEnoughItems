package mezz.jei.gui.config;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.core.config.sorting.MappedSortingConfig;
import mezz.jei.core.config.sorting.serializers.SortingSerializers;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.nio.file.Path;
import java.util.Comparator;

public class IngredientTypeSortingConfig extends MappedSortingConfig<IListElementInfo<?>, String> {
	public IngredientTypeSortingConfig(Path path) {
		super(path, SortingSerializers.STRING, IngredientTypeSortingConfig::getIngredientTypeString);
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
		String itemStackIngredientType = getIngredientTypeString(VanillaTypes.ITEM_STACK);
		Comparator<String> itemStackFirst = Comparator.comparing((String s) -> s.equals(itemStackIngredientType)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return itemStackFirst.thenComparing(naturalOrder);
	}

}
