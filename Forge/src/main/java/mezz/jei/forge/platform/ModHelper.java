package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModHelper implements IPlatformModHelper {
	private final Map<String, String> cache = new HashMap<>();
	private final Map<String, Optional<byte[]>> modIconCache = new HashMap<>();

	@Override
	public String getModNameForModId(String modId) {
		return cache.computeIfAbsent(modId, this::computeModNameForModId);
	}

	private String computeModNameForModId(String modId) {
		return ModList.get()
			.getModContainerById(modId)
			.map(ModContainer::getModInfo)
			.map(IModInfo::getDisplayName)
			.orElseGet(() -> StringUtils.capitalize(modId));
	}

	@Override
	public Optional<byte[]> getModIconBytes(String modId) {
		return modIconCache.computeIfAbsent(modId, this::computeModIconBytes);
	}

	private Optional<byte[]> computeModIconBytes(String modId) {
		return ModList.get()
			.getModContainerById(modId)
			.map(ModContainer::getModInfo)
			.flatMap(ModHelper::readModIconBytes);
	}

	private static Optional<byte[]> readModIconBytes(IModInfo modInfo) {
		return modInfo.getLogoFile()
			.filter(StringUtils::isNotBlank)
			.map(logoFile -> modInfo.getOwningFile()
				.getFile()
				.findResource(logoFile))
			.flatMap(ModHelper::readAllBytes);
	}

	private static Optional<byte[]> readAllBytes(Path path) {
		try {
			return Optional.of(Files.readAllBytes(path));
		} catch (IOException | RuntimeException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean isInDev() {
		return !FMLLoader.isProduction();
	}
}
