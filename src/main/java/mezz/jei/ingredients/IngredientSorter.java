package mezz.jei.ingredients;

import mezz.jei.config.IClientConfig;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;

import java.util.Comparator;
import java.util.List;

public final class IngredientSorter implements IIngredientSorter {
	private static final Comparator<IIngredientListElementInfo<?>> PRE_SORTED =
		Comparator.comparing(IIngredientListElementInfo::getSortedIndex);

	private final IClientConfig clientConfig;
	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;

	private boolean isCacheValid;

	public IngredientSorter(IClientConfig clientConfig, ModNameSortingConfig modNameSortingConfig, IngredientTypeSortingConfig ingredientTypeSortingConfig) {
		this.clientConfig = clientConfig;
		this.modNameSortingConfig = modNameSortingConfig;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
		this.isCacheValid = false;
	}

	@Override
	public void doPreSort(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients) {
		IngredientSorterComparators comparators = new IngredientSorterComparators(ingredientFilter, registeredIngredients, this.modNameSortingConfig, this.ingredientTypeSortingConfig);

		List<IngredientSortStage> ingredientSorterStages = this.clientConfig.getIngredientSorterStages();

		Comparator<IIngredientListElementInfo<?>> completeComparator = comparators.getComparator(ingredientSorterStages);

		// Get all of the items sorted with our custom comparator.
		List<IIngredientListElementInfo<?>> results = ingredientFilter.getIngredientListPreSort(completeComparator);

		// Go through all of the items and set their sorted index.
		for (int i = 0, resultsSize = results.size(); i < resultsSize; i++) {
			IIngredientListElementInfo<?> element = results.get(i);
			element.setSortedIndex(i);
		}
		this.isCacheValid = true;
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, RegisteredIngredients registeredIngredients) {
		if (!this.isCacheValid) {
			doPreSort(ingredientFilter, registeredIngredients);
		}
		//Now the comparator just uses that index value to order everything.
		return PRE_SORTED;
	}

	@Override
	public void invalidateCache() {
		this.isCacheValid = false;
	}

}
