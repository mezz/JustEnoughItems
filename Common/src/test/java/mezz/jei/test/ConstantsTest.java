package mezz.jei.test;

import mezz.jei.common.Constants;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class ConstantsTest {
	@Test
	public void debugTextureLocationExists() {
		ResourceLocation location = Constants.LOCATION_JEI_DEBUG_TEXTURE;
		String resourcePath = "/assets/%s/%s".formatted(location.getNamespace(), location.getPath());

		URL resource = ConstantsTest.class.getResource(resourcePath);

		Assertions.assertNotNull(resource, () -> "Missing debug texture at " + resourcePath);
	}
}
