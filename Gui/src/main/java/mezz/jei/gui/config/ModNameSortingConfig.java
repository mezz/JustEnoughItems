package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.legacy.LegacySortingConfigMigrator;
import mezz.jei.gui.ingredients.IListElementInfo;
import net.mezzdev.config.api.IConfigRegistration;
import net.mezzdev.config.api.sorting.ISortingConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class ModNameSortingConfig {
	private static final String CONFIG_FILE_NAME = "ingredient-list-mod-sort-order.ini";
	private static final boolean ALLOWS_REMOVING_VALUES = false;

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

	public ModNameSortingConfig(ISortingConfig<String> sortingConfig) {
		this.sortingConfig = Objects.requireNonNull(sortingConfig);
	}

	public Comparator<IListElementInfo<?>> getComparatorFromMappedValues(Collection<String> modNames) {
		Comparator<String> comparator = sortingConfig.getComparator(modNames);
		return Comparator.comparing(IListElementInfo::getModNameForSorting, comparator);
	}

	private static Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}
}
