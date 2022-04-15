package mezz.jei.fabric.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.fabric.mixin.KeyboardHandlerAccess;
import mezz.jei.fabric.mixin.KeyboardHandlerMixin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;

public class InputHelper implements IPlatformInputHelper {
    @Override
    public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key) {
        return !keyMapping.isUnbound() && keyMapping.matches(key.getValue(), 0);
    }

    @Override
    public boolean isSendRepeatsToGui(KeyboardHandler keyboardHandler) {
        var access = (KeyboardHandlerAccess) keyboardHandler;
        return access.getSendRepeatsToGui();
    }
}
