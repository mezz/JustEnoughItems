package mezz.jei.fabric.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import mezz.jei.fabric.mixin.KeyboardHandlerAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;

public class InputHelper implements IPlatformInputHelper {
    @Override
    public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key) {
        if (keyMapping.isUnbound()) {
            return false;
        }
        if (key.getType().equals(InputConstants.Type.MOUSE)) {
            return keyMapping.matchesMouse(key.getValue());
        }
        return keyMapping.matches(key.getValue(), 0);
    }

    @Override
    public boolean isSendRepeatsToGui(KeyboardHandler keyboardHandler) {
        var access = (KeyboardHandlerAccess) keyboardHandler;
        return access.getSendRepeatsToGui();
    }

    @Override
    public IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name) {
        return new FabricJeiKeyMappingCategoryBuilder(name);
    }
}
