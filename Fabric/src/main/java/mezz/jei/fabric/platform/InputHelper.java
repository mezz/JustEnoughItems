package mezz.jei.fabric.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public class InputHelper implements IPlatformInputHelper {
	@Override
	public boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input) {
		if (keyMapping.isUnbound()) {
			return false;
		}
		return input.ifKeyboardEvent((keyCode, scanCode, modifiers) -> keyMapping.matches(keyCode, scanCode)) ||
			input.ifMouseEvent((mouseX, mouseY, mouseButton) -> keyMapping.matchesMouse(mouseButton));
	}

	@Override
	public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name) {
		return new FabricJeiKeyMappingCategoryBuilder(name);
	}
}
