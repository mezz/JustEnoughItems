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
			case "bookmarks" -> BookmarkManagerClientGameTest.register();
			case "fluidIngredients" -> FluidIngredientGameTests.register();
			case "recipeSync" -> JeiFabricClientRecipeSyncTests.register();
			case "creativeInventory" -> JeiFabricCreativeInventoryClientGameTest.register();
			case "keyMapping" -> FabricClientTestRunner.register(List.of(
				JeiFabricClientResourceTests.getTestCase(),
				JeiFabricKeyMappingClientTests.getTestCase()
			));
			case "withoutAmecs" -> FabricClientTestRunner.register(List.of(
				JeiFabricClientResourceTests.getTestCase(),
				RecipeButtonsClientTest.getTestCase(),
				JeiFabricKeyMappingClientTests.getTestCase()
			));
			case "resources" -> FabricClientTestRunner.register(JeiFabricClientResourceTests.getTestCase());
			case "interactiveTooltips" -> FabricClientTestRunner.register("fabric-client-interactive-tooltips", "input", () -> {
				try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
					InteractiveIngredientTooltipClientGameTest.run();
				}
			});
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
						"Unknown JEI Fabric client test suite '" + testSuite + "'. Expected one of: all, bookmarks, fluidIngredients, recipeSync, creativeInventory, keyMapping, resources, withoutAmecs, interactiveTooltips, keyboardNavigation, guiResize"
					);
				}
			);
		}
	}

	private static void registerAllTests() {
		List<FabricClientTestRunner.ClientTestCase> testCases = new ArrayList<>();
		testCases.add(JeiFabricClientResourceTests.getTestCase());
		testCases.add(ConfigScreenClientTest.getTestCase());
		testCases.add(IngredientNavigationClientTest.getTestCase());
		testCases.add(RecipeButtonsClientTest.getTestCase());
		testCases.addAll(JeiFabricClientRecipeSyncTests.getTestCases());
		testCases.add(FluidIngredientGameTests.getTestCase());
		testCases.add(JeiFabricCreativeInventoryClientGameTest.getTestCase());
		testCases.add(JeiFabricKeyMappingClientTests.getTestCase());
		FabricClientTestRunner.register(testCases);
	}
}
