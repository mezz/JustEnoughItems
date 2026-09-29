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
		return input.ifKeyboardEvent(keyMapping::matches) ||
			input.ifMouseEvent((event, ignoredDoubleClick) -> keyMapping.matchesMouse(event));
	}

	@Override
	public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category) {
		return new FabricJeiKeyMappingCategoryBuilder(category);
	}
}
