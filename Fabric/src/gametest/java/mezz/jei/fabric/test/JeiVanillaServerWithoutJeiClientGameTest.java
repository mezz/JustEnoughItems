package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;

/**
 * Verifies fallback recipes and the server's pause-menu dialog on a vanilla connection.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiVanillaServerWithoutJeiClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport(
			"fabric-client-gametest",
			getClass().getSimpleName(),
			() -> {
				try (ExternalTestServer server = ExternalTestServer.startVanillaWithDialogDatapack(context);
					ExternalTestServer.Connection ignored = server.connect()
				) {
					JeiFabricClientGameTestAssertions.assertJeiStartedWithFallbackRecipes(context);
					JeiFabricClientGameTestAssertions.assertVanillaServer(context);
					assertPauseMenuDialogButton(context);
				}
				JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "vanilla server without JEI");
			}
		);
	}

	private static void assertPauseMenuDialogButton(ClientGameTestContext context) {
		context.waitFor(client -> client.screen == null, ClientGameTestContext.DEFAULT_TIMEOUT);
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitFor(client -> client.screen instanceof PauseScreen, ClientGameTestContext.DEFAULT_TIMEOUT);

		boolean hasDialogButton = context.computeOnClient(client -> client.screen instanceof PauseScreen pauseScreen && pauseScreen.children().stream()
			.anyMatch(child -> child instanceof Button button && button.visible && button.getMessage().getString().equals("Server Quick Commands"))
		);
		if (!hasDialogButton) {
			throw new AssertionError("Expected the vanilla server's Server Quick Commands button in the pause menu after JEI loaded fallback recipes.");
		}
	}
}
