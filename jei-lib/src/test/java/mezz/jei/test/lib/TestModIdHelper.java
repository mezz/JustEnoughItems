package mezz.jei.test.lib;

import javax.annotation.Nullable;

import mezz.jei.startup.AbstractModIdHelper;

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
