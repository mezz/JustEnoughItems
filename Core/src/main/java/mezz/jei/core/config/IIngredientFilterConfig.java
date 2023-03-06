package mezz.jei.core.config;

import mezz.jei.core.search.SearchMode;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getCreativeTabSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	boolean getSearchAdvancedTooltips();
}
