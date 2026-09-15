package mezz.jei.common.config.legacy;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public final class LegacyConfigPaths {
	private LegacyConfigPaths() {}

	public static List<Path> get(Path jeiConfigDirectory, @Nullable UUID profileId, String fileName) {
		jeiConfigDirectory = Objects.requireNonNull(jeiConfigDirectory).toAbsolutePath().normalize();
		Objects.requireNonNull(fileName);

		Path legacyFile = jeiConfigDirectory.resolve(fileName);
		if (profileId == null) {
			return List.of(legacyFile);
		}

		Path legacyProfileFile = jeiConfigDirectory.resolve("players").resolve(profileId.toString()).resolve(fileName);
		return List.of(legacyProfileFile, legacyFile);
	}
}
