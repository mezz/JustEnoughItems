package mezz.jei.fabric.test;

import mezz.jei.test.client.FakeMenuClientTest;
import net.minecraft.world.level.GameType;

final class FakeMenuClientGameTest {
	static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase("fabric-client-recipe-sync", "fakeMenuPlayerReplacement", () -> {
			try (FabricClientTestWorld ignored = FabricClientTestWorld.create(GameType.CREATIVE)) {
				FabricClientTestContext context = new FabricClientTestContext();
				FakeMenuClientTest.run(action -> context.runOnClient(action::accept), context::waitFor);
			}
		});
	}
}
