package mezz.jei.config;

import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.ingredients.IngredientInformation;

/** Visibility settings for ingredients hidden through the configuration. */
public class HideModeConfig implements IHideModeConfig {
	private final ConfigValues values;
	private final Set<String> blacklist;

	public HideModeConfig(ConfigValues values, Set<String> blacklist) {
		this.values = values;
		this.blacklist = blacklist;
	}

	@Override
	public boolean isHideModeEnabled() {
		return values.editModeEnabled;
	}

	@Override
	public <V> boolean isIngredientOnConfigBlacklist(V ingredient, IIngredientHelper<V> helper) {
		for (String uid : IngredientInformation.getUniqueIdsWithWildcard(helper, ingredient)) {
			if (blacklist.contains(uid)) {
				return true;
			}
		}
		return false;
	}
}
