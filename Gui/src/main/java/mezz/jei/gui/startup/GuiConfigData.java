package mezz.jei.gui.startup;

import mezz.jei.common.platform.Services;
import mezz.jei.gui.config.BookmarkJsonConfig;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.ILookupHistoryConfig;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.JeiGuiSortingConfigData;
import mezz.jei.gui.config.LookupHistoryJsonConfig;
import mezz.jei.gui.config.ModNameSortingConfig;

import java.nio.file.Path;

public record GuiConfigData(
	IBookmarkConfig bookmarkConfig,
	ILookupHistoryConfig lookupHistoryConfig,
	ModNameSortingConfig modNameSortingConfig,
	IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
	public static GuiConfigData create(JeiGuiSortingConfigData sortingConfigData) {
		Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();

		IBookmarkConfig bookmarkConfig = new BookmarkJsonConfig(configDir);
		ILookupHistoryConfig lookupHistoryConfig = new LookupHistoryJsonConfig(configDir);
		ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(sortingConfigData.ingredientModNameSortingConfig());
		IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(sortingConfigData.ingredientTypeSortingConfig());

		return new GuiConfigData(
			bookmarkConfig,
			lookupHistoryConfig,
			ingredientModNameSortingConfig,
			ingredientTypeSortingConfig
		);
	}
}
