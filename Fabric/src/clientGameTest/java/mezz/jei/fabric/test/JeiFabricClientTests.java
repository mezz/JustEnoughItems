package mezz.jei.fabric.test;

import mezz.jei.test.client.SpriteRenderingTest;
import net.fabricmc.api.ClientModInitializer;

import java.time.Duration;
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
			case "textInput" -> FabricClientTestRunner.register(JeiFabricKeyMappingClientTests.getTextInputTestCase());
			case "keyboardNavigation" -> FabricClientTestRunner.register("fabric-client-keyboard-navigation", "ingredientList", () -> {
				try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
					IngredientKeyboardNavigationClientGameTest.run();
					SearchInputCaptureClientTest.run();
				}
			});
			case "guiResize" -> FabricClientTestRunner.register("fabric-client-gui-resize", "resize", () -> {
				try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
					JeiGuiResizeClientGameTest.run();
				}
			});
			default -> FabricClientTestRunner.register(
				"fabric-client-tests",
				"unknown-test-suite",
				() -> {
					throw new IllegalArgumentException(
						"Unknown JEI Fabric client test suite '" + testSuite + "'. Expected one of: all, recipeSync, creativeInventory, fluidIngredients, keyMapping, textInput, keyboardNavigation, guiResize"
					);
				}
			);
		}
	}

	private static void registerAllTests() {
		List<FabricClientTestRunner.ClientTestCase> testCases = new ArrayList<>();
		testCases.add(new FabricClientTestRunner.ClientTestCase("fabric-client-gametest", "configGuiKeyModifiers", ConfigGuiKeyModifiersClientGameTest::run));
		testCases.add(new FabricClientTestRunner.ClientTestCase("fabric-client-gametest", "quickConfig", QuickConfigClientGameTest::run));
		testCases.add(ItemBatchRenderingClientGameTest.getTestCase());
		testCases.add(new FabricClientTestRunner.ClientTestCase("fabric-client-rendering", "arrowSprites", () -> ClientTestUtil.runOnClient(SpriteRenderingTest::run, Duration.ofSeconds(30))));
		testCases.add(IngredientDecoratorClientGameTest.getTestCase());
		testCases.add(FakeMenuClientGameTest.getTestCase());
		testCases.add(ChatItemLinksClientGameTest.getTestCase());
		testCases.addAll(JeiFabricClientRecipeSyncTests.getTestCases());
		testCases.add(JeiFabricCreativeInventoryClientGameTest.getTestCase());
		testCases.add(FluidIngredientGameTests.getTestCase());
		testCases.add(JeiFabricKeyMappingClientTests.getTestCase());
		FabricClientTestRunner.register(testCases);
	}
}
