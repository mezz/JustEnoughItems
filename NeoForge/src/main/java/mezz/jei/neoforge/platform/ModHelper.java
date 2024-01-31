package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
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

	@Override
	public boolean isInDev() {
		return !FMLLoader.isProduction();
	}
}
