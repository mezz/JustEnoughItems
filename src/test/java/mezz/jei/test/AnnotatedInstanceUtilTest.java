package mezz.jei.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.startup.AnnotatedInstanceUtil;
import org.junit.Assert;
import org.junit.Test;

public class AnnotatedInstanceUtilTest {
	@Test
	public void validPluginsLoadInOrder() {
		List<String> pluginClassNames = Arrays.asList(
			FirstValidPlugin.class.getName(),
			SecondValidPlugin.class.getName()
		);

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getInstances(pluginClassNames, IModPlugin.class);

		Assert.assertEquals(2, plugins.size());
		Assert.assertTrue(plugins.get(0) instanceof FirstValidPlugin);
		Assert.assertTrue(plugins.get(1) instanceof SecondValidPlugin);
	}

	@Test
	public void brokenPluginClassesAreSkippedWithoutStoppingDiscovery() {
		List<String> pluginClassNames = Arrays.asList(
			ConstructorThrowsPlugin.class.getName(),
			ThrowsDuringDiscoveryPlugin.class.getName(),
			InvalidPluginClass.class.getName(),
			NoDefaultConstructorPlugin.class.getName(),
			PrivateConstructorPlugin.class.getName(),
			"missing.test.JeiPlugin",
			FirstValidPlugin.class.getName()
		);

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getInstances(pluginClassNames, IModPlugin.class);

		Assert.assertEquals(1, plugins.size());
		Assert.assertTrue(plugins.get(0) instanceof FirstValidPlugin);
	}

	@Test
	public void emptyPluginListLoadsNothing() {
		List<IModPlugin> plugins = AnnotatedInstanceUtil.getInstances(Collections.emptyList(), IModPlugin.class);

		Assert.assertTrue(plugins.isEmpty());
	}

	public static class FirstValidPlugin implements IModPlugin {
	}

	public static class SecondValidPlugin implements IModPlugin {
	}

	public static class ConstructorThrowsPlugin implements IModPlugin {
		public ConstructorThrowsPlugin() {
			throw new IllegalStateException("plugin constructor failed");
		}
	}

	public static class ThrowsDuringDiscoveryPlugin implements IModPlugin {
		static {
			if (Boolean.TRUE) {
				throw new NoClassDefFoundError("missing/test/PluginDependency");
			}
		}
	}

	public static class NoDefaultConstructorPlugin implements IModPlugin {
		public NoDefaultConstructorPlugin(String value) {

		}
	}

	public static class PrivateConstructorPlugin implements IModPlugin {
		private PrivateConstructorPlugin() {

		}
	}

	public static class InvalidPluginClass {
	}
}
