package mezz.jei.fabric.test;

import mezz.jei.test.client.SpriteRenderingTest;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/** Verifies pixel-aligned arrow output from the client GUI renderer. */
public final class SpriteRenderingClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-rendering", "arrowSprites", () -> context.runOnClient(SpriteRenderingTest::run));
	}
}
