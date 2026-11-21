package mezz.jei.fabric.test;

import mezz.jei.test.client.FakeMenuClientTest;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

@SuppressWarnings("UnstableApiUsage")
public class FakeMenuClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
				.adjustSettings(settings -> settings.setAllowCommands(true)).create()
			) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				FakeMenuClientTest.run(action -> context.runOnClient(action::accept), condition -> context.waitFor(condition, 60 * 20));
			}
		});
	}
}
