package mezz.jei.gui.ingredients;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;

import java.util.Comparator;
import java.util.List;

public final class IngredientSorter implements IIngredientSorter {
	private static final Comparator<IListElementInfo<?>> PRE_SORTED =
		Comparator.comparing(IListElementInfo::getSortedIndex);

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
	public void doPreSort(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		IngredientSorterComparators comparators = new IngredientSorterComparators(ingredientFilter, ingredientManager, this.modNameSortingConfig, this.ingredientTypeSortingConfig);

		List<IngredientSortStage> ingredientSorterStages = this.clientConfig.getIngredientSorterStages();

		Comparator<IListElementInfo<?>> completeComparator = comparators.getComparator(ingredientSorterStages);

		// Get all of the items sorted with our custom comparator.
		List<IListElementInfo<?>> results = ingredientFilter.getIngredientListPreSort(completeComparator);

		// Go through all of the items and set their sorted index.
		for (int i = 0, resultsSize = results.size(); i < resultsSize; i++) {
			IListElementInfo<?> element = results.get(i);
			element.setSortedIndex(i);
		}
		this.isCacheValid = true;
	}

	@Override
	public Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		if (!this.isCacheValid) {
			doPreSort(ingredientFilter, ingredientManager);
		}
		//Now the comparator just uses that index value to order everything.
		return PRE_SORTED;
	}

	@Override
	public void invalidateCache() {
		this.isCacheValid = false;
	}

}
