package mezz.jei.api.config;

public interface IIngredientFilterConfig {
	SearchMode getModNameSearchMode();

	SearchMode getTooltipSearchMode();

	SearchMode getTagSearchMode();

	SearchMode getCreativeTabSearchMode();

	SearchMode getColorSearchMode();

	SearchMode getResourceLocationSearchMode();

	boolean getSearchAdvancedTooltips();
}
