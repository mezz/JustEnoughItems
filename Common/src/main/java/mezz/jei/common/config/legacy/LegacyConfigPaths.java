package mezz.jei.common.config.legacy;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LegacyConfigPaths {
	private LegacyConfigPaths() {}

	public static List<Path> get(Path jeiConfigDirectory, UUID profileId, String fileName) {
		jeiConfigDirectory = Objects.requireNonNull(jeiConfigDirectory).toAbsolutePath().normalize();
		Objects.requireNonNull(profileId);
		Objects.requireNonNull(fileName);

		Path legacyProfileFile = jeiConfigDirectory.resolve("players").resolve(profileId.toString()).resolve(fileName);
		Path legacyFile = jeiConfigDirectory.resolve(fileName);
		return List.of(legacyProfileFile, legacyFile);
	}
}
