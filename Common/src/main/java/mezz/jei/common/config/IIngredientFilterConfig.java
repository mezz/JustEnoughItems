package mezz.jei.common.config;

import mezz.jei.core.search.SearchMode;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	SearchMode getCreativeTabSearchMode();

	boolean getSearchAdvancedTooltips();

	boolean getSearchModIds();

	boolean getSearchIngredientAliases();

	boolean getSearchShortModNames();
}
