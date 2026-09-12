package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModHelper implements IPlatformModHelper {
	private final Map<String, String> cache = new HashMap<>();
	private final Map<String, List<byte[]>> modIconCache = new HashMap<>();

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
	public List<byte[]> getModIconByteCandidates(String modId) {
		return modIconCache.computeIfAbsent(modId, this::computeModIconBytes);
	}

	private List<byte[]> computeModIconBytes(String modId) {
		return FabricLoader.getInstance()
			.getModContainer(modId)
			.flatMap(modContainer -> modContainer.getMetadata()
				.getIconPath(64)
				.flatMap(modContainer::findPath))
			.flatMap(ModHelper::readAllBytes)
			.stream()
			.toList();
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
		FabricLoader loader = FabricLoader.getInstance();
		return loader.isDevelopmentEnvironment();
	}
}
