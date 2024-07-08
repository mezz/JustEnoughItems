package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ModHelper implements IPlatformModHelper {
	private final Map<String, String> cache = new HashMap<>();

	@Override
	public String getModNameForModId(String modId) {
		return cache.computeIfAbsent(modId, this::computeModNameForModId);
	}

	private String computeModNameForModId(String modId) {
		return FabricLoader.getInstance()
			.getModContainer(modId)
			.map(ModContainer::getMetadata)
			.map(ModMetadata::getName)
			.orElseGet(() -> StringUtils.capitalize(modId));
	}

	@Override
	public boolean isInDev() {
		FabricLoader loader = FabricLoader.getInstance();
		return loader.isDevelopmentEnvironment();
	}
}
