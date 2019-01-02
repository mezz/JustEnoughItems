package mezz.jei.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fml.common.ProgressManager;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.load.PluginLoader;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import org.junit.Assert;
import org.junit.Test;

public class PluginLoaderTest {
	@Test
	public void failedPluginDoesNotPreventLaterIngredientRegistration() {
		IModPlugin broken = new IModPlugin() {
			@Override
			public void registerIngredients(IModIngredientRegistration registry) {
				throw new NoClassDefFoundError("Missing optional mod dependency");
			}
		};
		IModPlugin working = new TestPlugin();
		List<IModPlugin> plugins = new ArrayList<>(Arrays.asList(broken, working));

		ModIngredientRegistration registration = PluginLoader.registerIngredients(plugins);
		IngredientRegistry registry = registration.createIngredientRegistry(new TestModIdHelper(), new IngredientBlacklistInternal());

		Assert.assertEquals(Collections.singletonList(working), plugins);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, registry.getAllIngredients(TestIngredient.TYPE).size());
	}

	@Test
	public void fatalVanillaRegistrationFailureClosesTheForgeProgressBar() {
		RuntimeException failure = new IllegalStateException("Vanilla registration failed");
		IModPlugin broken = new VanillaPlugin() {
			@Override
			public void registerIngredients(IModIngredientRegistration registry) {
				throw failure;
			}
		};
		List<ProgressManager.ProgressBar> before = progressBars();
		RuntimeException thrown = null;

		try {
			PluginLoader.registerIngredients(new ArrayList<>(Collections.singletonList(broken)));
		} catch (RuntimeException e) {
			thrown = e;
		}

		Assert.assertSame(failure, thrown);
		Assert.assertEquals(before, progressBars());
	}

	private static List<ProgressManager.ProgressBar> progressBars() {
		List<ProgressManager.ProgressBar> bars = new ArrayList<>();
		Iterator<ProgressManager.ProgressBar> iterator = ProgressManager.barIterator();
		iterator.forEachRemaining(bars::add);
		return bars;
	}
}
