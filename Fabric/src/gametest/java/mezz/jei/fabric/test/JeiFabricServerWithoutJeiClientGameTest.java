package mezz.jei.fabric.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Verifies Fabric-server detection and fallback recipes when JEI is missing from the server.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricServerWithoutJeiClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (ExternalTestServer server = ExternalTestServer.startFabricWithoutJei(context);
			ExternalTestServer.Connection ignored = server.connect()
		) {
			JeiFabricClientGameTestAssertions.assertJeiStartedWithFallbackRecipes(context);
			JeiFabricClientGameTestAssertions.assertServerMissingJei(context);
		}
		JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "Fabric server without JEI");
	}
}
