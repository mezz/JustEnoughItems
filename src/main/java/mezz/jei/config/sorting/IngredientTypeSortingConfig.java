package mezz.jei.config.sorting;

import mezz.jei.config.sorting.serializers.SortingSerializers;
import mezz.jei.ingredients.IIngredientListElementInfo;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.util.Comparator;

public class IngredientTypeSortingConfig extends MappedSortingConfig<IIngredientListElementInfo<?>, String> {
	public IngredientTypeSortingConfig(File file) {
		super(file, SortingSerializers.STRING, IngredientTypeSortingConfig::getIngredientType);
	}

	public static String getIngredientType(IIngredientListElementInfo<?> info) {
		Object ingredient = info.getIngredient();
		Class<?> ingredientClass = ingredient.getClass();
		return getIngredientType(ingredientClass);
	}

	public static String getIngredientType(Class<?> ingredientClass) {
		return ingredientClass.getName();
	}

	@Override
	protected Comparator<String> getDefaultSortOrder() {
		String itemStackIngredientType = getIngredientType(ItemStack.class);
		Comparator<String> itemStackFirst = Comparator.comparing((String s) -> s.equals(itemStackIngredientType)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return itemStackFirst.thenComparing(naturalOrder);
	}

}
