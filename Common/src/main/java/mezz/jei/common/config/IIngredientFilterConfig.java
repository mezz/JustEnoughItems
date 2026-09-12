package mezz.jei.common.config;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getCreativeTabSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	boolean getSearchAdvancedTooltips();

	boolean getSearchModIds();

	boolean getSearchModAliases();

	boolean getSearchIngredientAliases();

	boolean getSearchShortModNames();

	void addSearchConfigListener(Runnable listener);
}
