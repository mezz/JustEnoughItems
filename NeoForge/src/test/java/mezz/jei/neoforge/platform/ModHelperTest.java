package mezz.jei.neoforge.platform;

import net.neoforged.neoforgespi.language.IConfigurable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModHelperTest {
	@Test
	void getModIconFilesIncludesLegacyFallbacks() {
		IConfigurable modConfig = new TestConfigurable(Map.of(
			"iconFile", "mod-icon.png",
			"bannerFile", "mod-banner.png"
		));
		IConfigurable fileConfig = new TestConfigurable(Map.of(
			"iconFile", "file-icon.png",
			"logoFile", "file-logo.png",
			"bannerFile", "file-banner.png"
		));

		List<String> iconFiles = ModHelper.getModIconFiles(
			modConfig,
			fileConfig,
			Optional.of("legacy-logo.png")
		);

		assertEquals(List.of(
			"mod-icon.png",
			"file-icon.png",
			"legacy-logo.png",
			"file-logo.png",
			"mod-banner.png",
			"file-banner.png"
		), iconFiles);
	}

	private record TestConfigurable(Map<String, String> values) implements IConfigurable {
		@Override
		@SuppressWarnings("unchecked")
		public <T> Optional<T> getConfigElement(String... key) {
			if (key.length != 1) {
				return Optional.empty();
			}
			return Optional.ofNullable((T) values.get(key[0]));
		}

		@Override
		public List<? extends IConfigurable> getConfigList(String... key) {
			return List.of();
		}
	}
}
