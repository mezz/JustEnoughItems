package mezz.jei.config;

import java.util.Set;
import java.util.function.BooleanSupplier;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.ingredients.IngredientInformation;

/** Visibility settings for ingredients hidden through the configuration. */
public class HideModeConfig implements IHideModeConfig {
	private final BooleanSupplier editModeEnabled;
	private final Set<String> blacklist;

	public HideModeConfig(ConfigValues values, Set<String> blacklist) {
		this(() -> values.editModeEnabled, blacklist);
	}

	public HideModeConfig(BooleanSupplier editModeEnabled, Set<String> blacklist) {
		this.editModeEnabled = editModeEnabled;
		this.blacklist = blacklist;
	}

	@Override
	public boolean isHideModeEnabled() {
		return editModeEnabled.getAsBoolean();
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
