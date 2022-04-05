package mezz.jei.forge.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.platform.IPlatformInputHelper;
import net.minecraft.client.KeyMapping;

public class ForgeInputHelper implements IPlatformInputHelper {
    @Override
    public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key) {
        return keyMapping.isActiveAndMatches(key);
    }
}
