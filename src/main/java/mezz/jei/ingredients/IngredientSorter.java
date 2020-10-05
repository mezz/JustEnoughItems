package mezz.jei.ingredients;

import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public final class IngredientSorter implements IIngredientSorter {

	private static final Comparator<IIngredientListElementInfo<?>> ITEM_STACK_FIRST =
		Comparator.comparing((IIngredientListElementInfo<?> o) -> {
			IIngredientListElement<?> element = o.getElement();
			Object ingredient = element.getIngredient();
			return ingredient instanceof ItemStack;
		}).reversed();

	private static final Comparator<IIngredientListElementInfo<?>> CREATIVE =
		Comparator.comparingInt(o -> {
			IIngredientListElement<?> element = o.getElement();
			return element.getOrderIndex();
		});

	private static final Comparator<IIngredientListElementInfo<?>> ALPHABETICAL =
		Comparator.comparing(IIngredientListElementInfo::getName);

	private Comparator<IIngredientListElementInfo<?>> modId;
	private Comparator<IIngredientListElementInfo<?>> comparator;

	public IngredientSorter(ModNameSortingConfig modNameSortingConfig) {
		List<String> ordering = modNameSortingConfig.getSorted();
		this.modId = createModIdComparator(ordering);
		this.comparator = createComparator(this.modId);
		modNameSortingConfig.addListener(this::updateModIdSorting);
	}

	private void updateModIdSorting(List<String> ordering) {
		modId = createModIdComparator(ordering);
		comparator = createComparator(modId);
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator() {
		return comparator;
	}

	private static Comparator<IIngredientListElementInfo<?>> createModIdComparator(List<String> ordering) {
		return Comparator.comparingInt(o -> {
			String modNameForSorting = o.getModNameForSorting();
			return ordering.indexOf(modNameForSorting);
		});
	}

	private static Comparator<IIngredientListElementInfo<?>> createComparator(Comparator<IIngredientListElementInfo<?>> modId) {
		return modId.thenComparing(ITEM_STACK_FIRST).thenComparing(CREATIVE);
	}
}
