package mezz.jei.forge.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.forge.input.ForgeJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;

public class InputHelper implements IPlatformInputHelper {
    @Override
    public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key) {
        return keyMapping.isActiveAndMatches(key);
    }

    @Override
    public boolean isSendRepeatsToGui(KeyboardHandler keyboardHandler) {
        return keyboardHandler.sendRepeatsToGui;
    }

    @Override
    public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name) {
        return new ForgeJeiKeyMappingCategoryBuilder(name);
    }
}
