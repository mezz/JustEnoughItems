package mezz.jei.fabric.test;

import net.fabricmc.api.ClientModInitializer;

import java.util.ArrayList;
import java.util.List;

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
			case "all" -> registerAllTests();
			case "recipeSync" -> JeiFabricClientRecipeSyncTests.register();
			case "creativeInventory" -> JeiFabricCreativeInventoryClientGameTest.register();
			case "fluidIngredients" -> FluidIngredientGameTests.register();
			case "keyMapping" -> JeiFabricKeyMappingClientTests.register();
			default -> FabricClientTestRunner.register(
				"fabric-client-tests",
				"unknown-test-suite",
				() -> {
					throw new IllegalArgumentException(
						"Unknown JEI Fabric client test suite '" + testSuite + "'. Expected one of: all, recipeSync, creativeInventory, fluidIngredients, keyMapping"
					);
				}
			);
		}
	}

	private static void registerAllTests() {
		List<FabricClientTestRunner.ClientTestCase> testCases = new ArrayList<>();
		testCases.addAll(JeiFabricClientRecipeSyncTests.getTestCases());
		testCases.add(JeiFabricCreativeInventoryClientGameTest.getTestCase());
		testCases.add(FluidIngredientGameTests.getTestCase());
		testCases.add(JeiFabricKeyMappingClientTests.getTestCase());
		FabricClientTestRunner.register(testCases);
	}
}
