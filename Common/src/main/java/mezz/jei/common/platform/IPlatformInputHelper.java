package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;

public interface IPlatformInputHelper {
    boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key);

    boolean isSendRepeatsToGui(KeyboardHandler keyboardHandler);

    IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name);
}
