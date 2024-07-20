package mezz.jei.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.load.PluginCaller;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluginCallerTest {
	@Test
	public void keepsBrokenPluginsForLaterRegistrationStages() {
		IModPlugin plugin = () -> new ResourceLocation("test", "broken");
		List<IModPlugin> plugins = new ArrayList<>(Collections.singletonList(plugin));

		PluginCaller.callOnPlugins("test", plugins, ignored -> {
			throw new IllegalStateException("expected test failure");
		});

		assertEquals(Collections.singletonList(plugin), plugins);
	}
}
