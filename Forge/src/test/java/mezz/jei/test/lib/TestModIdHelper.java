package mezz.jei.test.lib;

import mezz.jei.ingredients.AbstractModIdHelper;

public class TestModIdHelper extends AbstractModIdHelper {
	@Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		return getModNameForModId(modId);
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		return true;
	}
}
