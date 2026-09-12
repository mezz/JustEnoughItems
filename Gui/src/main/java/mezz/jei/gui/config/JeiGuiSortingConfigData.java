package mezz.jei.gui.config;

import net.mezzdev.config.api.sorting.ISortingConfig;

public record JeiGuiSortingConfigData(
	ISortingConfig<String> ingredientModNameSortingConfig,
	ISortingConfig<String> ingredientTypeSortingConfig
) {
}
