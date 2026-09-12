package mezz.jei.common.config;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	SearchMode getCreativeTabSearchMode();

	boolean getSearchAdvancedTooltips();

	boolean getSearchModIds();

	boolean getSearchModAliases();

	boolean getSearchIngredientAliases();

	boolean getSearchShortModNames();

	void addSearchConfigListener(Runnable listener);
}
