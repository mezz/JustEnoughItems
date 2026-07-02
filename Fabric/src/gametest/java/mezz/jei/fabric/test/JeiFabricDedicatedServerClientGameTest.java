package mezz.jei.fabric.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;

@SuppressWarnings("UnstableApiUsage")
public class JeiFabricDedicatedServerClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestDedicatedServerContext server = context.worldBuilder().createServer();
			 TestServerConnection connection = server.connect()) {
			connection.getClientLevel().waitForChunksRender();

			JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
		}
	}
}
