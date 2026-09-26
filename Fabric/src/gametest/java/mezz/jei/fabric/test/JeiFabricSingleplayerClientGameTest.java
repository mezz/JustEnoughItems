package mezz.jei.fabric.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.mezzdev.config.gui.ConfigGui;
import net.mezzdev.config.gui.fabric.ConfigGuiFabricPluginFinder;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;

/**
 * Verifies recipe syncing and config screen rendering before and after joining singleplayer.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricSingleplayerClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReportWithBooleanVariant(
			"fabric-client-gametest",
			"jei.fabric.disableAmecsSupport",
			"without-amecs",
			getClass().getSimpleName(),
			() -> {
				assertConfigScreenRenders(context, "jei-config-menu");
				try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
					JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
					JeiFabricClientGameTestAssertions.assertServerHasJei(context);
					ConfigScreenClientTest.run(context);
					assertConfigScreenRenders(context, "jei-config-world");
					RecipeSlotTooltipClientTest.run(context);
					RecipeGuiScrollingClientTest.run(context);
				}
				JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "Fabric singleplayer");
			}
		);
	}

	private static void assertConfigScreenRenders(ClientGameTestContext context, String screenshotName) {
		Screen parent = context.computeOnClient(client -> client.gui.screen());
		try {
			context.setScreen(() -> {
				var factories = ConfigGui.createScreenFactories(ConfigGuiFabricPluginFinder.getPlugins());
				var factory = Objects.requireNonNull(factories.get(ModIds.JEI_ID), "Expected JEI's config screen to be available.");
				return factory.create(parent);
			});
			context.runOnClient(client -> ConfigScreenClientTest.assertCategoryLayout(Objects.requireNonNull(client.gui.screen())));
			context.takeScreenshot(screenshotName);
			ConfigScreenClientTest.assertAlignmentSearchRenders(context, screenshotName);
			ConfigScreenClientTest.captureOrganizedCategories(context, screenshotName);
		} finally {
			context.setScreen(() -> parent);
		}
	}
}
