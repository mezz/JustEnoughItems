package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class IngredientSorter implements IIngredientSorter {

	private static final Comparator<IIngredientListElementInfo<?>> CREATIVE_MENU =
		Comparator.comparingInt(o -> {
			IIngredientListElement<?> element = o.getElement();
			return element.getOrderIndex();
		});

	private static final Comparator<IIngredientListElementInfo<?>> ALPHABETICAL =
		Comparator.comparing(IIngredientListElementInfo::getName);

	private final IClientConfig clientConfig;
	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;

	@Nullable
	private Comparator<IIngredientListElementInfo<?>> cachedComparator;

	public IngredientSorter(IClientConfig clientConfig, ModNameSortingConfig modNameSortingConfig, IngredientTypeSortingConfig ingredientTypeSortingConfig) {
		this.clientConfig = clientConfig;
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

			EnumMap<IngredientSortStage, Comparator<IIngredientListElementInfo<?>>> comparatorsForStages = new EnumMap<>(IngredientSortStage.class);
			comparatorsForStages.put(IngredientSortStage.ALPHABETICAL, ALPHABETICAL);
			comparatorsForStages.put(IngredientSortStage.CREATIVE_MENU, CREATIVE_MENU);
			comparatorsForStages.put(IngredientSortStage.INGREDIENT_TYPE, ingredientType);
			comparatorsForStages.put(IngredientSortStage.MOD_NAME, modName);

			List<IngredientSortStage> ingredientSorterStages = this.clientConfig.getIngredientSorterStages();
			this.cachedComparator = ingredientSorterStages.stream()
				.map(comparatorsForStages::get)
				.reduce(Comparator::thenComparing)
				.orElseGet(() -> modName.thenComparing(ingredientType).thenComparing(CREATIVE_MENU));
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
