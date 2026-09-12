package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformModHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ModHelper implements IPlatformModHelper {
	private final Map<String, String> cache = new HashMap<>();
	private final Map<String, List<byte[]>> modIconCache = new HashMap<>();

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
	public List<byte[]> getModIconByteCandidates(String modId) {
		return modIconCache.computeIfAbsent(modId, this::computeModIconBytes);
	}

	private List<byte[]> computeModIconBytes(String modId) {
		return ModList.get()
			.getModContainerById(modId)
			.map(ModContainer::getModInfo)
			.map(ModHelper::readModIconBytes)
			.orElseGet(List::of);
	}

	private static List<byte[]> readModIconBytes(IModInfo modInfo) {
		JarContents contents = modInfo.getOwningFile()
			.getFile()
			.getContents();
		return getModIconFiles(
				modInfo.getConfig(),
				modInfo.getOwningFile().getConfig(),
				modInfo.getLogoFile()
			)
			.stream()
			.map(path -> readAllBytes(contents, path))
			.flatMap(Optional::stream)
			.toList();
	}

	static List<String> getModIconFiles(
		IConfigurable modConfig,
		IConfigurable fileConfig,
		Optional<String> legacyLogoFile
	) {
		return Stream.of(
				modConfig.<String>getConfigElement("iconFile"),
				fileConfig.<String>getConfigElement("iconFile"),
				legacyLogoFile,
				fileConfig.<String>getConfigElement("logoFile"),
				modConfig.<String>getConfigElement("bannerFile"),
				fileConfig.<String>getConfigElement("bannerFile")
			)
			.flatMap(Optional::stream)
			.filter(StringUtils::isNotBlank)
			.distinct()
			.toList();
	}

	private static Optional<byte[]> readAllBytes(JarContents contents, String path) {
		try {
			return Optional.ofNullable(contents.readFile(path));
		} catch (IOException | RuntimeException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean isInDev() {
		FMLLoader fmlLoader = FMLLoader.getCurrentOrNull();
		return fmlLoader != null && !fmlLoader.isProduction();
	}
}
