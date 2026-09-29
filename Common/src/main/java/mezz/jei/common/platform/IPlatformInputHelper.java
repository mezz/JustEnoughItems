package mezz.jei.common.platform;

import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public interface IPlatformInputHelper {
	boolean isActiveAndMatches(KeyMapping keyMapping, UserInput input);

	IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name);
}
