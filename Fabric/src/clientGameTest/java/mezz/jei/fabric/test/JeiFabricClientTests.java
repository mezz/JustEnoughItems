package mezz.jei.fabric.test;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side test mod entrypoint for Fabric coverage.
 */
public final class JeiFabricClientTests implements ClientModInitializer {
	private static final String TEST_SUITE_PROPERTY = "jei.fabric.clientTest";

	@Override
	public void onInitializeClient() {
		String testSuite = System.getProperty(TEST_SUITE_PROPERTY);
		if (testSuite == null) {
			return;
		}

		switch (testSuite) {
			case "recipeSync" -> JeiFabricClientRecipeSyncTests.register();
			case "keyMapping" -> JeiFabricKeyMappingClientTests.register();
			default -> FabricClientTestRunner.register(
				"fabric-client-tests",
				"unknown-test-suite",
				() -> {
					throw new IllegalArgumentException(
						"Unknown JEI Fabric client test suite '" + testSuite + "'. Expected one of: recipeSync, keyMapping"
					);
				}
			);
		}
	}
}
