package mezz.jei.common.config;

import mezz.jei.core.search.SearchMode;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	boolean getSearchAdvancedTooltips();
}
