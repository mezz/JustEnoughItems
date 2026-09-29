package mezz.jei.forge.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.forge.input.ForgeJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public class InputHelper implements IPlatformInputHelper {
	@Override
	public boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input) {
		return keyMapping.isActiveAndMatches(input.getKey());
	}

	@Override
	public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name) {
		return new ForgeJeiKeyMappingCategoryBuilder(name);
	}
}
