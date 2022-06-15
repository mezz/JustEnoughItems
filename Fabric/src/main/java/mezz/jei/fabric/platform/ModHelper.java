package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.lang3.StringUtils;

public class ModHelper implements IPlatformModHelper {
    @Override
    public String getModNameForModId(String modId) {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.getModContainer(modId)
            .map(ModContainer::getMetadata)
            .map(ModMetadata::getName)
            .orElseGet(() -> StringUtils.capitalize(modId));
    }
}
