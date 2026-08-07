package mezz.jei.debug;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DebugResourcesTest {
	@Test
	public void backgroundTextureExists() {
		ResourceLocation location = DebugRecipeCategory.BACKGROUND_TEXTURE;
		String resourcePath = "/assets/%s/%s".formatted(location.getNamespace(), location.getPath());

		URL resource = DebugResourcesTest.class.getResource(resourcePath);

		Assertions.assertNotNull(resource, () -> "Missing debug texture at " + resourcePath);
	}
}
