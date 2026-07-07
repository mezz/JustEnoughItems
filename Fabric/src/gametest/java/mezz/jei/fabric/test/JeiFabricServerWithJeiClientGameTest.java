package mezz.jei.fabric.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Verifies that JEI uses synced recipes when a Fabric client connects to a Fabric server with JEI.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricServerWithJeiClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (ExternalTestServer server = ExternalTestServer.startFabricWithJei(context);
			ExternalTestServer.Connection ignored = server.connect()
		) {
			JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
			JeiFabricClientGameTestAssertions.assertServerHasJei(context);
		}
		JeiFabricClientGameTestAssertions.assertClientRecipesCleared(context, "Fabric server with JEI");
	}
}
