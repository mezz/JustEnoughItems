package mezz.jei.fabric.test;

import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Verifies that JEI uses synced recipes when a Fabric client joins singleplayer.
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
				try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
					JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
					JeiFabricClientGameTestAssertions.assertServerHasJei(context);
				}
				JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "Fabric singleplayer");
			}
		);
	}
}
