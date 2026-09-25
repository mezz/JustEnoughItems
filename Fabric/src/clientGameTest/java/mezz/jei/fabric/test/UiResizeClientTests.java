package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import java.time.Duration;

final class UiResizeClientTests {
	private UiResizeClientTests() {}

	static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(
			"fabric-client-gui", "resizing", UiResizeClientTests::run
		);
	}

	private static void run() {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				Duration.ofSeconds(60),
				() -> "Timed out waiting for JEI to start for GUI resizing"
			);
			JeiGuiResizeClientGameTest.run();
		}
	}
}
