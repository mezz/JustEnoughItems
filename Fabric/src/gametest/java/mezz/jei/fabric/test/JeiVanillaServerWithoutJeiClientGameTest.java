package mezz.jei.fabric.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Verifies fallback recipes when a Fabric client connects to a vanilla server.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiVanillaServerWithoutJeiClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (ExternalTestServer server = ExternalTestServer.startVanilla(context);
			ExternalTestServer.Connection ignored = server.connect()
		) {
			JeiFabricClientGameTestAssertions.assertJeiStartedWithFallbackRecipes(context);
			JeiFabricClientGameTestAssertions.assertVanillaServer(context);
		}
		JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "vanilla server without JEI");
	}
}
