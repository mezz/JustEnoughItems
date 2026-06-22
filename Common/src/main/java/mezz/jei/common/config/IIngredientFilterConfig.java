package mezz.jei.common.config;

import mezz.jei.common.search.SearchMode;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getIdentifierSearchMode();

	SearchMode getCreativeTabSearchMode();

	boolean getSearchAdvancedTooltips();

	boolean getSearchModIds();

	boolean getSearchModAliases();

	boolean getSearchIngredientAliases();

	boolean getSearchShortModNames();
}
