package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.core.config.IngredientSortStage;

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
	public void doPreSort(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients) {
		IngredientSorterComparators comparators = new IngredientSorterComparators(ingredientFilter, registeredIngredients, this.modNameSortingConfig, this.ingredientTypeSortingConfig);

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
	public Comparator<IListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IRegisteredIngredients registeredIngredients) {
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
