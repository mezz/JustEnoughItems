package mezz.jei.test.lib;

import mezz.jei.core.search.SearchMode;
import mezz.jei.common.config.IIngredientFilterConfig;

public class TestIngredientFilterConfig implements IIngredientFilterConfig {
	@Override
	public SearchMode getModNameSearchMode() {
		return SearchMode.ENABLED;
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return SearchMode.ENABLED;
	}

	@Override
	public SearchMode getTagSearchMode() {
		return SearchMode.ENABLED;
	}

	@Override
	public SearchMode getColorSearchMode() {
		// TODO enable testing color search
		return SearchMode.DISABLED;
	}

	@Override
	public SearchMode getResourceLocationSearchMode() {
		return SearchMode.ENABLED;
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return false;
	}
}
