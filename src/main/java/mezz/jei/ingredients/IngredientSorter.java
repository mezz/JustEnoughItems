package mezz.jei.ingredients;

import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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

	private final ModNameSortingConfig modNameSortingConfig;

	@Nullable
	private Comparator<IIngredientListElementInfo<?>> comparator;

	public IngredientSorter(ModNameSortingConfig modNameSortingConfig) {
		this.modNameSortingConfig = modNameSortingConfig;
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator(Set<String> allValues) {
		if (this.comparator == null) {
			List<String> ordering = this.modNameSortingConfig.getSorted(allValues);
			Comparator<IIngredientListElementInfo<?>> modName = createModNameComparator(ordering);
			this.comparator = createComparator(modName);
		}
		return this.comparator;
	}

	@Override
	public void invalidateCache() {
		this.comparator = null;
	}

	private static Comparator<IIngredientListElementInfo<?>> createModNameComparator(List<String> ordering) {
		return Comparator.comparingInt(o -> {
			String modNameForSorting = o.getModNameForSorting();
			int index = ordering.indexOf(modNameForSorting);
			if (index < 0) {
				index = Integer.MAX_VALUE;
			}
			return index;
		});
	}

	private static Comparator<IIngredientListElementInfo<?>> createComparator(Comparator<IIngredientListElementInfo<?>> modName) {
		return modName.thenComparing(ITEM_STACK_FIRST).thenComparing(CREATIVE);
	}
}
