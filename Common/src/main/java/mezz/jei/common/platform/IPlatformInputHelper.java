package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public interface IPlatformInputHelper {
    boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key);

    IJeiKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(String name);
}
