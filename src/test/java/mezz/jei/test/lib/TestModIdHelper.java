package mezz.jei.test.lib;

import mezz.jei.startup.AbstractModIdHelper;

public class TestModIdHelper extends AbstractModIdHelper {
	@Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}
}
