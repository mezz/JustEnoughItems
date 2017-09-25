package mezz.jei.test.lib;

import mezz.jei.startup.AbstractModIdHelper;

import javax.annotation.Nullable;

public class TestModIdHelper extends AbstractModIdHelper {
	@Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		return getModNameForModId(modId);
	}

	@Nullable
	@Override
	public String getModNameTooltipFormatting() {
		return null;
	}
}
