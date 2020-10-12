package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public final class IngredientSorter implements IIngredientSorter {

	private static final Comparator<IIngredientListElementInfo<?>> CREATIVE =
		Comparator.comparingInt(o -> {
			IIngredientListElement<?> element = o.getElement();
			return element.getOrderIndex();
		});

	private static final Comparator<IIngredientListElementInfo<?>> ALPHABETICAL =
		Comparator.comparing(IIngredientListElementInfo::getName);

	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;

	@Nullable
	private Comparator<IIngredientListElementInfo<?>> comparator;

	public IngredientSorter(ModNameSortingConfig modNameSortingConfig, IngredientTypeSortingConfig ingredientTypeSortingConfig) {
		this.modNameSortingConfig = modNameSortingConfig;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator(Collection<String> modNames, Collection<IIngredientType<?>> ingredientTypes) {
		if (this.comparator == null) {
			Comparator<IIngredientListElementInfo<?>> modName = this.modNameSortingConfig.getComparatorFromMappedValues(modNames);

			Set<String> ingredientTypeStrings = ingredientTypes.stream()
				.map(IIngredientType::getIngredientClass)
				.map(IngredientTypeSortingConfig::getIngredientType)
				.collect(Collectors.toSet());
			Comparator<IIngredientListElementInfo<?>> ingredientType = this.ingredientTypeSortingConfig.getComparatorFromMappedValues(ingredientTypeStrings);

			this.comparator = modName.thenComparing(ingredientType).thenComparing(CREATIVE);
		}
		return this.comparator;
	}

	@Override
	public void invalidateCache() {
		this.comparator = null;
	}

}
