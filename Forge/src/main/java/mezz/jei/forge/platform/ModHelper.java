package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;

public class ModHelper implements IPlatformModHelper {
    @Override
    public String getModNameForModId(String modId) {
        ModList modList = ModList.get();
        return modList.getModContainerById(modId)
            .map(ModContainer::getModInfo)
            .map(IModInfo::getDisplayName)
            .orElseGet(() -> StringUtils.capitalize(modId));
    }
}
