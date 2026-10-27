package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.Constants;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.TextInputTestUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Verifies JEI's Fabric key-mapping wrappers with and without AMECS support.
 *
 * <p>This test class is run by two Fabric client launches:
 * <ul>
 *     <li>{@code :Fabric:runClientGameTest} includes this test with AMECS and its JEI integration enabled.</li>
 *     <li>{@code :Fabric:runClientGameTestWithoutAmecs} removes AMECS from the runtime classpath to exercise JEI's plain Fabric key-mapping path.</li>
 * </ul>
 */
final class JeiFabricKeyMappingClientTests {
	private static final String JUNIT_SUITE_NAME = "fabric-client-key-mapping";
	private static final String TEST_NAME = "JeiFabricKeyMappingClientTests";
	private static final String CATEGORY = "key.categories.jei.test.key_mapping";
	private static final ResourceLocation GUI_BACKGROUND_TEXTURE = new ResourceLocation("jei", "textures/gui/sprites/gui_background.png");
	private static final String FOCUS_SEARCH_TRANSLATION_KEY = "key.jei.focusSearch";
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);

	private JeiFabricKeyMappingClientTests() {

	}

	public static void register() {
		FabricClientTestRunner.register(getTestCase());
	}

	public static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(JUNIT_SUITE_NAME, TEST_NAME, JeiFabricKeyMappingClientTests::runTest);
	}

	private static void runTest() {
		ClientTestUtil.runOnClient(JeiFabricKeyMappingClientTests::assertJeiResourcesAreLoaded);
		FabricKeyModifiersClientGameTest.run();
		assertFocusSearchHotkeyDoesNotTypeItsCharacter();
	}

	private static void assertJeiResourcesAreLoaded(Minecraft client) {
		ResourceLocation slotId = new ResourceLocation("jei", "gui/sprites/slot");
		var texture = client.getTextureManager().getTexture(Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);
		if (!(texture instanceof TextureAtlas atlas)) {
			throw new AssertionError("Expected JEI's GUI texture atlas to be loaded");
		}
		if (!atlas.getSprite(slotId).getName().equals(slotId)) {
			int resourceCount = client.getResourceManager()
				.listResources("textures/gui/sprites", id -> id.getPath().endsWith(".png"))
				.size();
			throw new AssertionError("Expected JEI's slot texture to be stitched in JEI's GUI atlas; found " + resourceCount + " source textures.");
		}
		if (client.getResourceManager().getResource(GUI_BACKGROUND_TEXTURE).isEmpty()) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common textures.");
		}
		if (!Language.getInstance().has(FOCUS_SEARCH_TRANSLATION_KEY)) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common translations.");
		}
	}

	private static void assertFocusSearchHotkeyDoesNotTypeItsCharacter() {
		AtomicReference<Runnable> restoreBinding = new AtomicReference<>(() -> {});
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for JEI to start in the Fabric client test world."
			);

			ClientTestUtil.runOnClient(client -> client.setScreen(null));
			KeyMapping inventory = ClientTestUtil.computeOnClient(client -> client.options.keyInventory);
			FabricClientTestInput.pressKey(inventory);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.screen instanceof InventoryScreen),
				ASSERTION_TIMEOUT,
				() -> ClientTestUtil.computeOnClient(client -> {
					String screenName = "none";
					if (client.screen != null) {
						screenName = client.screen.getClass().getName();
					}
					return "Timed out opening the inventory; current screen: " + screenName;
				})
			);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getJeiRuntime().getIngredientListOverlay().isListDisplayed()),
				ASSERTION_TIMEOUT,
				() -> ClientTestUtil.computeOnClient(client -> "Timed out displaying JEI at " +
					client.getWindow().getGuiScaledWidth() + "x" + client.getWindow().getGuiScaledHeight())
			);

			ClientTestUtil.runOnClient(client -> {
				KeyMapping focusSearch = Arrays.stream(client.options.keyMappings)
					.filter(mapping -> mapping.getName().equals("key.jei.focusSearch"))
					.findFirst()
					.orElseThrow(() -> new AssertionError("Expected the focus-search key mapping to be registered."));
				// Use the same Options API as the Controls screen. Unbinding first also clears old AMECS modifiers.
				restoreBinding.set(KeyMappingTestHelper.rebindUnmodified(focusSearch, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F)));
				Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
			});

			FabricClientTestInput.pressKey(GLFW.GLFW_KEY_F);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getJeiRuntime().getIngredientListOverlay().hasKeyboardFocus()),
				ASSERTION_TIMEOUT,
				() -> "Timed out focusing JEI search with its Fabric hotkey."
			);

			FabricClientTestInput.typeChar('f');
			String filterText = ClientTestUtil.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().getFilterText());
			if (!filterText.isEmpty()) {
				throw new AssertionError("Expected the focus-search hotkey character to be consumed, got: " + filterText);
			}

			ClientTestUtil.runOnClient(client -> {
				IngredientListOverlay overlay = (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
				GuiTextFieldFilter searchField = new ReflectionUtil().getFieldWithClass(overlay, GuiTextFieldFilter.class)
					.findFirst().orElseThrow(() -> new AssertionError("Expected JEI's search field."));
				assertShortcutCharacters(client.keyboardHandler, client.getWindow().getWindow(), searchField);
			});
		} finally {
			ClientTestUtil.runOnClient(client -> restoreBinding.get().run());
		}
	}

	private static void assertShortcutCharacters(KeyboardHandler keyboard, long window, GuiTextFieldFilter searchField) {
		for (String text : List.of("", "stone")) {
			for (int codePoint : new int[]{'f', 0x1F600}) {
				searchField.setValue(text);
				searchField.clearFocus();
				TextInputTestUtil.invokeKeyPress(keyboard, window, GLFW.GLFW_KEY_F);
				if (!searchField.isFocused()) {
					throw new AssertionError("Expected the focus shortcut to select JEI search.");
				}
				TextInputTestUtil.invokeCharacterCallback(keyboard, window, codePoint);
				if (!searchField.getValue().equals(text)) {
					throw new AssertionError("Expected the focus shortcut to preserve '" + text + "', got: " + searchField.getValue());
				}
				searchField.setValue("");
				TextInputTestUtil.invokeKeyPress(keyboard, window, GLFW.GLFW_KEY_F);
				TextInputTestUtil.invokeCharacterCallback(keyboard, window, 'f');
				if (!searchField.getValue().equals("f")) {
					throw new AssertionError("Expected the bound letter to type normally when search already has focus.");
				}
				searchField.setValue("");
				TextInputTestUtil.typePlainText(keyboard, window, searchField);
			}
		}

		KeyMapping previousSearch = KeyMappingTestHelper.find("key.jei.previousSearch");
		Runnable restoreHistory = KeyMappingTestHelper.rebindUnmodified(previousSearch, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G));
		try {
			searchField.setValue("stone");
			searchField.clearFocus();
			searchField.focus();
			searchField.setValue("dirt");
			TextInputTestUtil.invokeKeyPress(keyboard, window, GLFW.GLFW_KEY_G);
			if (!searchField.isFocused() || !searchField.getValue().equals("stone")) {
				throw new AssertionError("Expected the history shortcut to recall 'stone' while keeping search focused.");
			}
			TextInputTestUtil.invokeCharacterCallback(keyboard, window, 'g');
			if (!searchField.getValue().equals("stone")) {
				throw new AssertionError("Expected the history shortcut's character to be consumed, got: " + searchField.getValue());
			}
			searchField.setValue("");
			TextInputTestUtil.typePlainText(keyboard, window, searchField);
		} finally {
			restoreHistory.run();
		}

		KeyMapping focusSearch = KeyMappingTestHelper.find(FOCUS_SEARCH_TRANSLATION_KEY);
		Runnable restoreFocus = KeyMappingTestHelper.rebindUnmodified(focusSearch, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F6));
		try {
			searchField.clearFocus();
			TextInputTestUtil.invokeKeyPress(keyboard, window, GLFW.GLFW_KEY_F6);
			if (!searchField.isFocused()) {
				throw new AssertionError("Expected the nonprinting focus shortcut to select JEI search.");
			}
			TextInputTestUtil.typePlainText(keyboard, window, searchField);
		} finally {
			restoreFocus.run();
		}
	}

}
