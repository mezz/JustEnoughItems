package mezz.jei.gui.config;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.legacy.LegacySortingConfigMigrator;
import mezz.jei.gui.ingredients.IListElementInfo;
import net.mezzdev.config.api.IConfigRegistration;
import net.mezzdev.config.api.sorting.ISortingConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class IngredientTypeSortingConfig {
	private static final String CONFIG_FILE_NAME = "ingredient-list-type-sort-order.ini";
	private static final boolean ALLOWS_REMOVING_VALUES = true;

	private final ISortingConfig<String> sortingConfig;

	public static ISortingConfig<String> create(IConfigRegistration registration) {
		return registration.createSortingConfig(
			CONFIG_FILE_NAME,
			getDefaultSortOrder(),
			ALLOWS_REMOVING_VALUES
		);
	}

	public static ISortingConfig<String> create(
		IConfigRegistration registration,
		Path jeiConfigDirectory,
		UUID profileId
	) {
		ISortingConfig<String> sortingConfig = create(registration);
		return LegacySortingConfigMigrator.register(sortingConfig, jeiConfigDirectory, profileId, CONFIG_FILE_NAME);
	}

	public IngredientTypeSortingConfig(ISortingConfig<String> sortingConfig) {
		this.sortingConfig = Objects.requireNonNull(sortingConfig);
	}

	public static String getIngredientTypeString(IListElementInfo<?> info) {
		ITypedIngredient<?> typedIngredient = info.getTypedIngredient();
		return getIngredientTypeString(typedIngredient.getType());
	}

	public static String getIngredientTypeString(IIngredientType<?> ingredientType) {
		return ingredientType.getIngredientClass().getName();
	}

	public Comparator<IListElementInfo<?>> getComparatorFromMappedValues(Collection<String> ingredientTypeStrings) {
		Comparator<String> comparator = sortingConfig.getComparator(ingredientTypeStrings);
		return Comparator.comparing(IngredientTypeSortingConfig::getIngredientTypeString, comparator);
	}

	public Runnable addChangeListener(Runnable listener) {
		return sortingConfig.addChangeListener(listener);
	}

	private static Comparator<String> getDefaultSortOrder() {
		String itemStackIngredientType = getIngredientTypeString(VanillaTypes.ITEM_STACK);
		Comparator<String> itemStackFirst = Comparator.comparing((String s) -> s.equals(itemStackIngredientType)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return itemStackFirst.thenComparing(naturalOrder);
	}
}
