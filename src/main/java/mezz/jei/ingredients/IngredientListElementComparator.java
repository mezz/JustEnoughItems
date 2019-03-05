package mezz.jei.ingredients;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

import mezz.jei.api.constants.ModIds;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.ingredients.IIngredientListElementInfo;

public final class IngredientListElementComparator implements Comparator<IIngredientListElementInfo<?>> {
	public static final IngredientListElementComparator INSTANCE = new IngredientListElementComparator();

	private IngredientListElementComparator() {

	}

	@Override
	public int compare(IIngredientListElementInfo<?> o1, IIngredientListElementInfo<?> o2) {
		final String modName1 = o1.getModNameForSorting();
		final String modName2 = o2.getModNameForSorting();
		IIngredientListElement<?> element = o1.getElement();

		if (modName1.equals(modName2)) {
			boolean isItemStack1 = (element.getIngredient() instanceof ItemStack);
			boolean isItemStack2 = (element.getIngredient() instanceof ItemStack);
			if (isItemStack1 && !isItemStack2) {
				return -1;
			} else if (!isItemStack1 && isItemStack2) {
				return 1;
			}

			final int orderIndex1 = element.getOrderIndex();
			final int orderIndex2 = element.getOrderIndex();
			return Integer.compare(orderIndex1, orderIndex2);
		} else if (modName1.equals(ModIds.MINECRAFT_NAME)) {
			return -1;
		} else if (modName2.equals(ModIds.MINECRAFT_NAME)) {
			return 1;
		} else {
			return modName1.compareTo(modName2);
		}
	}
}
