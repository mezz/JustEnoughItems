package mezz.jei.neoforge.startup;

import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ForgePluginFinderTest {
	@Test
	public void validPluginsLoadInOrder() {
		// Setup: discovery found two valid plugin classes in scan order.
		List<String> pluginClassNames = List.of(
			FirstValidPlugin.class.getName(),
			SecondValidPlugin.class.getName()
		);

		// Operation: instantiate the plugin classes.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(pluginClassNames, IModPlugin.class);

		// Assertions: both plugins load in the same order.
		Assertions.assertEquals(2, plugins.size());
		Assertions.assertInstanceOf(FirstValidPlugin.class, plugins.get(0));
		Assertions.assertInstanceOf(SecondValidPlugin.class, plugins.get(1));
	}

	@Test
	public void brokenPluginClassesAreSkippedWithoutStoppingDiscovery() {
		// Setup: several broken declarations are followed by a valid plugin.
		List<String> pluginClassNames = List.of(
			ConstructorThrowsPlugin.class.getName(),
			ThrowsDuringDiscoveryPlugin.class.getName(),
			InvalidPluginClass.class.getName(),
			FirstValidPlugin.class.getName()
		);

		// Operation: instantiate plugins through the class-name path used after annotation scanning.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(pluginClassNames, IModPlugin.class);

		// Assertions: broken entries are skipped, and the later valid plugin still loads.
		Assertions.assertEquals(1, plugins.size());
		Assertions.assertInstanceOf(FirstValidPlugin.class, plugins.getFirst());
	}

	@Test
	public void emptyPluginListLoadsNothing() {
		// Setup and operation: instantiate an empty discovery result.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(List.of(), IModPlugin.class);

		// Assertions: no plugins are returned.
		Assertions.assertTrue(plugins.isEmpty());
	}

	@Test
	public void missingPluginClassIsSkipped() {
		// Setup: discovery returned a class name that is not present, followed by a valid plugin.
		List<String> pluginClassNames = List.of(
			"missing.test.JeiPlugin",
			FirstValidPlugin.class.getName()
		);

		// Operation: instantiate plugins from the discovered class names.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(pluginClassNames, IModPlugin.class);

		// Assertions: the missing class is skipped and the valid plugin still loads.
		Assertions.assertEquals(1, plugins.size());
		Assertions.assertInstanceOf(FirstValidPlugin.class, plugins.getFirst());
	}

	@Test
	public void pluginWithoutNoArgConstructorIsSkipped() {
		// Setup: a plugin class cannot be constructed with JEI's no-arg constructor path.
		List<String> pluginClassNames = List.of(
			NoDefaultConstructorPlugin.class.getName(),
			FirstValidPlugin.class.getName()
		);

		// Operation: instantiate plugins from the discovered class names.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(pluginClassNames, IModPlugin.class);

		// Assertions: the invalid constructor shape is skipped.
		Assertions.assertEquals(1, plugins.size());
		Assertions.assertInstanceOf(FirstValidPlugin.class, plugins.getFirst());
	}

	@Test
	public void pluginWithPrivateConstructorIsSkipped() {
		// Setup: a plugin class has a no-arg constructor, but JEI cannot access it.
		List<String> pluginClassNames = List.of(
			PrivateConstructorPlugin.class.getName(),
			FirstValidPlugin.class.getName()
		);

		// Operation: instantiate plugins from the discovered class names.
		List<IModPlugin> plugins = ForgePluginFinder.getInstances(pluginClassNames, IModPlugin.class);

		// Assertions: inaccessible constructors are skipped.
		Assertions.assertEquals(1, plugins.size());
		Assertions.assertInstanceOf(FirstValidPlugin.class, plugins.getFirst());
	}

	public static class FirstValidPlugin implements IModPlugin {
		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "first_valid_test_plugin");
		}
	}

	public static class SecondValidPlugin implements IModPlugin {
		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "second_valid_test_plugin");
		}
	}

	public static class ConstructorThrowsPlugin implements IModPlugin {
		public ConstructorThrowsPlugin() {
			throw new IllegalStateException("plugin constructor failed");
		}

		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "constructor_throws");
		}
	}

	public static class ThrowsDuringDiscoveryPlugin implements IModPlugin {
		static {
			if (Boolean.TRUE) {
				throw new NoClassDefFoundError("missing/test/PluginDependency");
			}
		}

		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "discovery_throws");
		}
	}

	public static class NoDefaultConstructorPlugin implements IModPlugin {
		public NoDefaultConstructorPlugin(String value) {

		}

		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "no_default_constructor");
		}
	}

	public static class PrivateConstructorPlugin implements IModPlugin {
		private PrivateConstructorPlugin() {

		}

		@Override
		public ResourceLocation getPluginUid() {
			return ResourceLocation.fromNamespaceAndPath("jei", "private_constructor");
		}
	}

	public static class InvalidPluginClass {

	}
}
