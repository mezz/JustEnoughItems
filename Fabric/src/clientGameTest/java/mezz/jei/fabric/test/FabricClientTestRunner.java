package mezz.jei.fabric.test;

import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

final class FabricClientTestRunner {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration CLIENT_STARTUP_TIMEOUT = Duration.ofSeconds(120);

	private FabricClientTestRunner() {

	}

	public static void register(String suiteName, String testName, JUnitXmlTestReporter.ThrowingRunnable test) {
		AtomicBoolean started = new AtomicBoolean(false);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (started.compareAndSet(false, true)) {
				Thread thread = new Thread(() -> runTests(suiteName, testName, test), "JEI Fabric Client Tests");
				thread.setDaemon(false);
				thread.start();
			}
		});
	}

	private static void runTests(String suiteName, String testName, JUnitXmlTestReporter.ThrowingRunnable test) {
		int exitCode = 0;
		try {
			waitForClientStartup();
			JUnitXmlTestReporter.runAndReport(
				suiteName,
				testName,
				test
			);
			LOGGER.info("JEI Fabric client test passed: {}.{}", suiteName, testName);
		} catch (Throwable t) {
			exitCode = 1;
			LOGGER.error("JEI Fabric client test failed: {}.{}", suiteName, testName, t);
		} finally {
			FabricClientTestInput.clear();
			stopClient(exitCode);
		}
	}

	private static void waitForClientStartup() {
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> client.getOverlay() == null && client.screen != null),
			CLIENT_STARTUP_TIMEOUT,
			() -> "Timed out waiting for Minecraft client startup before running Fabric client tests."
		);
	}

	private static void stopClient(int exitCode) {
		try {
			ClientTestUtil.runOnClient(Minecraft::stop);
		} catch (Throwable t) {
			exitCode = 1;
			LOGGER.error("Failed to stop Minecraft after JEI Fabric client tests.", t);
		}
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}
}
