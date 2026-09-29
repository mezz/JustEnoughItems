package mezz.jei.common.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;

public interface IPlatformInputHelper {
	boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input);

	boolean isSendRepeatsToGui(KeyboardHandler keyboardHandler);

	IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name);
}
