package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public interface IPlatformInputHelper {
    boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key);
}
