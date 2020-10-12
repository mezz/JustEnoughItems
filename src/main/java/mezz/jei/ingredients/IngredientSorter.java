package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
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
	private Comparator<IIngredientListElementInfo<?>> cachedComparator;

	public IngredientSorter(ModNameSortingConfig modNameSortingConfig, IngredientTypeSortingConfig ingredientTypeSortingConfig) {
		this.modNameSortingConfig = modNameSortingConfig;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		if (this.cachedComparator == null) {
			Set<String> modNames = ingredientFilter.getModNamesForSorting();
			Collection<IIngredientType<?>> ingredientTypes = ingredientManager.getRegisteredIngredientTypes();

			Comparator<IIngredientListElementInfo<?>> modName = createModNameComparator(modNames);
			Comparator<IIngredientListElementInfo<?>> ingredientType = createIngredientTypeComparator(ingredientTypes);

			this.cachedComparator = modName.thenComparing(ingredientType).thenComparing(CREATIVE);
		}
		return this.cachedComparator;
	}

	private Comparator<IIngredientListElementInfo<?>> createModNameComparator(Collection<String> modNames) {
		return this.modNameSortingConfig.getComparatorFromMappedValues(modNames);
	}

	private Comparator<IIngredientListElementInfo<?>> createIngredientTypeComparator(Collection<IIngredientType<?>> ingredientTypes) {
		Set<String> ingredientTypeStrings = ingredientTypes.stream()
			.map(IIngredientType::getIngredientClass)
			.map(IngredientTypeSortingConfig::getIngredientType)
			.collect(Collectors.toSet());
		return this.ingredientTypeSortingConfig.getComparatorFromMappedValues(ingredientTypeStrings);
	}

	@Override
	public void invalidateCache() {
		this.cachedComparator = null;
	}

}
