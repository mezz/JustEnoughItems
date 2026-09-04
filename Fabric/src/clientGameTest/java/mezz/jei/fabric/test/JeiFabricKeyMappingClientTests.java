package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.fabric.input.FabricJeiKeyMapping;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import mezz.jei.fabric.input.FabricKeyMapping;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Verifies JEI's Fabric key-mapping wrappers with and without AMECS support.
 *
 * <p>This test class is run by two Fabric client launches:
 * <ul>
 *     <li>{@code :Fabric:runClientGameTest} includes this test with AMECS and its JEI integration enabled.</li>
 *     <li>{@code :Fabric:runClientGameTestWithoutAmecs} removes AMECS from the runtime classpath and runs only this
 *     test to exercise JEI's plain Fabric key-mapping path.</li>
 * </ul>
 */
final class JeiFabricKeyMappingClientTests {
	private static final String JUNIT_SUITE_NAME = "fabric-client-key-mapping";
	private static final String TEST_NAME = "JeiFabricKeyMappingClientTests";
	private static final String CATEGORY = "key.categories.jei.test.key_mapping";
	private static final ResourceLocation GUI_BACKGROUND_TEXTURE = new ResourceLocation("jei", "textures/gui/gui_background.png");
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
		ClientTestUtil.runOnClient(client -> {
			assertJeiResourcesAreLoaded(client);
			assertModifierKeyPollingMatchesEitherSide();
			assertFabricKeyMappingConflictContexts();
			assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
				"key.jei.test.fabricKeyMapping.keyboardR",
				InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R),
				InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_T)
			);
			assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
				"key.jei.test.fabricKeyMapping.keyboardU",
				InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U),
				InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Y)
			);
			assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
				"key.jei.test.fabricKeyMapping.mouseLeft",
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT),
				InputConstants.Type.MOUSE.getOrCreate(3)
			);
			assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
				"key.jei.test.fabricKeyMapping.mouseRight",
				InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT),
				InputConstants.Type.MOUSE.getOrCreate(2)
			);
			if (FabricAmecsSupport.isEnabled()) {
				AmecsKeyMappingClientTestHelper.assertKeyMappingConflictContexts(CATEGORY);
				AmecsKeyMappingClientTestHelper.assertJeiKeyMappingIsDiscoverableAndRebindable(CATEGORY);
			}
		});
		assertModifiedJeiKeyMappings();
		assertFocusSearchHotkeyDoesNotTypeItsCharacter();
		assertJeiMouseMappingsDoNotHideVanillaMouseClicks();
	}

	private static void assertModifierKeyPollingMatchesEitherSide() {
		assertModifierKeyPollingMatchesEitherSide(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		assertModifierKeyPollingMatchesEitherSide(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
		assertModifierKeyPollingMatchesEitherSide(GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		assertModifierKeyPollingMatchesEitherSide(GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
	}

	private static void assertModifierKeyPollingMatchesEitherSide(int leftKeyValue, int rightKeyValue) {
		InputConstants.Key leftKey = InputConstants.Type.KEYSYM.getOrCreate(leftKeyValue);
		InputConstants.Key rightKey = InputConstants.Type.KEYSYM.getOrCreate(rightKeyValue);

		FabricClientTestInput.releaseKey(leftKeyValue);
		FabricClientTestInput.releaseKey(rightKeyValue);
		if (IJeiKeyMappingInternal.isKeyDown(leftKey)) {
			throw new AssertionError("Expected JEI modifier polling to reject a released key: " + leftKey.getName());
		}
		if (IJeiKeyMappingInternal.isKeyDown(rightKey)) {
			throw new AssertionError("Expected JEI modifier polling to reject a released key: " + rightKey.getName());
		}

		FabricClientTestInput.holdKey(rightKeyValue);
		try {
			if (!IJeiKeyMappingInternal.isKeyDown(leftKey)) {
				throw new AssertionError("Expected JEI modifier polling to accept the right-side key for: " + leftKey.getName());
			}
			if (!IJeiKeyMappingInternal.isKeyDown(rightKey)) {
				throw new AssertionError("Expected JEI modifier polling to accept the right-side key for: " + rightKey.getName());
			}
		} finally {
			FabricClientTestInput.releaseKey(rightKeyValue);
		}

		FabricClientTestInput.holdKey(leftKeyValue);
		try {
			if (!IJeiKeyMappingInternal.isKeyDown(leftKey)) {
				throw new AssertionError("Expected JEI modifier polling to accept the left-side key for: " + leftKey.getName());
			}
			if (!IJeiKeyMappingInternal.isKeyDown(rightKey)) {
				throw new AssertionError("Expected JEI modifier polling to accept the left-side key for: " + rightKey.getName());
			}
		} finally {
			FabricClientTestInput.releaseKey(leftKeyValue);
		}
	}

	private static void assertJeiResourcesAreLoaded(Minecraft client) {
		if (client.getResourceManager().getResource(GUI_BACKGROUND_TEXTURE).isEmpty()) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common textures.");
		}
		if (!Language.getInstance().has(FOCUS_SEARCH_TRANSLATION_KEY)) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common translations.");
		}
	}

	private static void assertFocusSearchHotkeyDoesNotTypeItsCharacter() {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for JEI to start in the Fabric client test world."
			);

			ClientTestUtil.runOnClient(client -> {
				client.options.pauseOnLostFocus = false;
				client.setWindowActive(true);
				client.setScreen(null);
			});
			KeyMapping inventory = ClientTestUtil.computeOnClient(client -> client.options.keyInventory);
			FabricClientTestInput.pressKey(inventory);
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.screen instanceof InventoryScreen),
				ASSERTION_TIMEOUT,
				() -> ClientTestUtil.computeOnClient(client -> "Timed out opening the inventory; current screen: " +
					(client.screen == null ? "none" : client.screen.getClass().getName()))
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
				client.options.setKey(focusSearch, InputConstants.UNKNOWN);
				client.options.setKey(focusSearch, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F));
				KeyMapping.resetMapping();
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

			FabricClientTestInput.pressKey(GLFW.GLFW_KEY_X);
			FabricClientTestInput.typeChar('x');
			filterText = ClientTestUtil.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().getFilterText());
			if (!filterText.equals("x")) {
				throw new AssertionError("Expected normal typing to resume after the focus-search character, got: " + filterText);
			}
		}
	}

	private static void assertFabricKeyMappingConflictContexts() {
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G);
		FabricKeyMapping guiMapping = createFabricKeyMapping("conflicts.gui", key, JeiKeyConflictContext.GUI);
		FabricKeyMapping otherGuiMapping = createFabricKeyMapping("conflicts.otherGui", key, JeiKeyConflictContext.GUI);
		FabricKeyMapping inGameMapping = createFabricKeyMapping("conflicts.inGame", key, JeiKeyConflictContext.IN_GAME);
		FabricKeyMapping universalMapping = createFabricKeyMapping("conflicts.universal", key, JeiKeyConflictContext.UNIVERSAL);
		KeyMapping regularMapping = new KeyMapping("key.jei.test.fabricKeyMapping.conflicts.regular", key.getType(), key.getValue(), CATEGORY);

		try {
			assertConflictContextBehavior("Fabric", guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		} finally {
			unbindMappings(guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		}
	}

	private static FabricKeyMapping createFabricKeyMapping(String nameSuffix, InputConstants.Key key, JeiKeyConflictContext context) {
		return new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping." + nameSuffix,
			key.getType(),
			key.getValue(),
			CATEGORY,
			context
		);
	}

	static void assertConflictContextBehavior(
		String implementationName,
		KeyMapping guiMapping,
		KeyMapping otherGuiMapping,
		KeyMapping inGameMapping,
		KeyMapping universalMapping,
		KeyMapping regularMapping
	) {
		if (!guiMapping.same(otherGuiMapping)) {
			throw new AssertionError("Expected " + implementationName + " mappings in the same context to conflict.");
		}
		if (guiMapping.same(inGameMapping) || inGameMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " GUI and in-game mappings not to conflict.");
		}
		if (!guiMapping.same(universalMapping) || !universalMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " universal mappings to conflict with every JEI context.");
		}
		if (!guiMapping.same(regularMapping) || !regularMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " and regular mappings to report key conflicts symmetrically.");
		}
	}

	static void unbindMappings(KeyMapping... mappings) {
		for (KeyMapping mapping : mappings) {
			mapping.setKey(InputConstants.UNKNOWN);
		}
		KeyMapping.resetMapping();
	}

	private static void assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
		String description,
		InputConstants.Key boundKey,
		InputConstants.Key reboundKey
	) {
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			description,
			boundKey.getType(),
			boundKey.getValue(),
			CATEGORY,
			JeiKeyConflictContext.UNIVERSAL
		);
		IJeiKeyMappingInternal jeiMapping = new FabricJeiKeyMapping<>(fabricMapping);
		assertJeiKeyMappingIsDiscoverableAndRebindable("Fabric", fabricMapping, jeiMapping, boundKey, reboundKey);
	}

	static void assertJeiKeyMappingIsDiscoverableAndRebindable(
		String implementationName,
		KeyMapping platformMapping,
		IJeiKeyMappingInternal jeiMapping,
		InputConstants.Key boundKey,
		InputConstants.Key reboundKey
	) {
		assertBoundKeyEquals(implementationName, platformMapping, boundKey, "initial binding");
		if (!jeiMapping.isActiveAndMatches(boundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to match its initial key: " + boundKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to reject the UNKNOWN key.");
		}

		platformMapping.setKey(reboundKey);
		KeyMapping.resetMapping();
		assertBoundKeyEquals(implementationName, platformMapping, reboundKey, "rebound binding");
		if (!jeiMapping.isActiveAndMatches(reboundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to match its rebound key: " + reboundKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(boundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to stop matching its previous key: " + boundKey.getName());
		}

		platformMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
		assertBoundKeyEquals(implementationName, platformMapping, InputConstants.UNKNOWN, "unbound binding");
		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to report unbound after setting UNKNOWN.");
		}
	}

	private static void assertBoundKeyEquals(
		String implementationName,
		KeyMapping platformMapping,
		InputConstants.Key expectedKey,
		String operation
	) {
		InputConstants.Key exposedKey = KeyBindingHelper.getBoundKeyOf(platformMapping);
		if (!exposedKey.equals(expectedKey)) {
			throw new AssertionError(
				"Expected Fabric's key helper to expose the " + implementationName + " " + operation + ": " + expectedKey.getName()
			);
		}
	}

	private static void assertModifiedJeiKeyMappings() {
		assertModifiedMapping(keyboardMapping("key.jei.test.modifiedKeyMapping.shiftKeyboardR", GLFW.GLFW_KEY_R), JeiKeyModifier.SHIFT);
		assertModifiedMapping(keyboardMapping("key.jei.test.modifiedKeyMapping.controlOrCommandKeyboardU", GLFW.GLFW_KEY_U), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(mouseLeftMapping("key.jei.test.modifiedKeyMapping.shiftMouseLeft"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(mouseRightMapping("key.jei.test.modifiedKeyMapping.shiftMouseRight"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(mouseLeftMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseLeft"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(mouseRightMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseRight"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(mouseRightMapping("key.jei.test.modifiedKeyMapping.altMouseRight"), JeiKeyModifier.ALT);
	}

	private static void assertJeiMouseMappingsDoNotHideVanillaMouseClicks() {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.runOnClient(client -> client.setScreen(null));
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> {
					// Setup: enter the in-game input state with no screen open and JEI's GUI context inactive.
					// JEI's GUI-only mouse mappings use the same buttons as vanilla attack/use, but vanilla
					// should remain responsible for those clicks while the player is in-world.
					if (client.level == null) {
						return false;
					}
					if (client.screen != null || JeiKeyConflictContext.GUI.isActive()) {
						client.setScreen(null);
						return false;
					}

					assertJeiKeyMappingHeldState();
					assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(client.options.keyAttack, client.options.keyUse);
					assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(client.options.keyAttack, client.options.keyUse);
					if (FabricAmecsSupport.isEnabled()) {
						AmecsKeyMappingClientTestHelper.assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(CATEGORY);
					}
					return true;
				}),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for the Fabric client to enter the in-game input state."
			);
		}
	}

	private static void assertJeiKeyMappingHeldState() {
		TestMapping mapping = createActiveTestMapping(
			"key.jei.test.heldKeyMapping.shift",
			GLFW.GLFW_KEY_LEFT_SHIFT,
			JeiKeyConflictContext.UNIVERSAL
		);

		if (mapping.jeiMapping().isDown()) {
			throw new AssertionError("Expected JEI's held-key mapping to start released.");
		}

		holdShiftForKeyMappingDispatch();
		try {
			if (!mapping.jeiMapping().isDown()) {
				throw new AssertionError("Expected JEI's held-key mapping to detect its standard bound key.");
			}
		} finally {
			releaseShiftForKeyMappingDispatch();
		}

		if (mapping.jeiMapping().isDown()) {
			throw new AssertionError("Expected JEI's held-key mapping to detect that its key was released.");
		}
		mapping.platformMapping().setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
	}

	private static TestMapping createActiveTestMapping(
		String description,
		int keyCode,
		JeiKeyConflictContext context
	) {
		IJeiKeyMappingInternal jeiMapping = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
			.createMapping(description)
			.setContext(context)
			.buildKeyboardKey(keyCode);
		AtomicReference<KeyMapping> platformMapping = new AtomicReference<>();
		jeiMapping.register(platformMapping::set);
		KeyMapping registeredMapping = platformMapping.get();
		if (registeredMapping == null) {
			throw new AssertionError("Expected JEI's Fabric key mapping to register its platform mapping.");
		}
		return new TestMapping(registeredMapping, jeiMapping);
	}

	private static void assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(KeyMapping keyAttack, KeyMapping keyUse) {
		// Setup: create bound JEI mouse mappings that should be active only in JEI/GUI contexts.
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);
		if (FabricAmecsSupport.isEnabled()) {
			AmecsKeyMappingClientTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
			AmecsKeyMappingClientTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
			AmecsKeyMappingClientTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
			AmecsKeyMappingClientTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
		}

		// Operation and assertions: bound JEI GUI-only mouse mappings must not hide vanilla attack/use clicks
		// while the player is in-world.
		assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click with bound JEI mouse mappings");
		assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click with bound JEI mouse mappings");
		holdShiftForKeyMappingDispatch();
		try {
			assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click with bound JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click with bound JEI mouse mappings");
		} finally {
			releaseShiftForKeyMappingDispatch();
		}
	}

	private static void assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(KeyMapping keyAttack, KeyMapping keyUse) {
		// Setup: create JEI mouse mappings on the same buttons as vanilla attack/use, then unbind them as the
		// Controls screen would. Unbound JEI mappings must not remain registered for click dispatch.
		assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
		assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);
		if (FabricAmecsSupport.isEnabled()) {
			AmecsKeyMappingClientTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
			AmecsKeyMappingClientTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
			AmecsKeyMappingClientTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
			AmecsKeyMappingClientTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
		}

		// Operation and assertions: setting JEI mouse bindings to UNKNOWN must remove them from vanilla/AMECS
		// click dispatch, so vanilla attack/use still receive the click.
		assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click after unbinding JEI mouse mappings");
		assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click after unbinding JEI mouse mappings");
		holdShiftForKeyMappingDispatch();
		try {
			assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click after unbinding JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click after unbinding JEI mouse mappings");
		} finally {
			releaseShiftForKeyMappingDispatch();
		}
	}

	private static void assertModifiedMapping(
		ModifiedMapping mapping,
		JeiKeyModifier modifier
	) {
		// Setup: build the mapping through JEI's Fabric category builder so AMECS support is selected once for
		// this client startup, the same way it is during normal JEI initialization.
		AtomicReference<KeyMapping> platformMapping = new AtomicReference<>();
		IJeiKeyMappingInternal jeiMapping = ClientTestUtil.computeOnClient(client -> {
			IJeiKeyMappingBuilder builder = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
				.createMapping(mapping.description())
				.setModifier(modifier);
			return mapping.mappingFactory()
				.build(builder)
				.register(platformMapping::set);
		});
		if (platformMapping.get() == null) {
			throw new AssertionError("Expected the modified JEI key mapping to register its platform mapping.");
		}

		// Operation: ask JEI whether the mapping accepts the real input key before and after holding the modifier.
		boolean isUnbound = ClientTestUtil.computeOnClient(client -> jeiMapping.isUnbound());
		boolean matchesWithoutModifier = ClientTestUtil.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
		FabricClientTestInput.holdModifier(modifier);
		boolean matchesWithModifier;
		boolean matchesUnknownKey;
		try {
			matchesWithModifier = ClientTestUtil.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
			matchesUnknownKey = ClientTestUtil.computeOnClient(client -> jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN));
		} finally {
			FabricClientTestInput.releaseModifier(modifier);
		}

		// Assertions: when AMECS support is enabled, the modified mapping stays bound and only matches with
		// its modifier held. When AMECS support is disabled, the modified mapping is unsupported and unbound.
		try {
			if (FabricAmecsSupport.isEnabled()) {
				if (isUnbound) {
					throw new AssertionError("Expected AMECS-backed modified key mapping to stay bound.");
				}
				if (matchesWithoutModifier) {
					throw new AssertionError("Expected modified key mapping to reject input when its modifier is not held.");
				}
				if (!matchesWithModifier) {
					throw new AssertionError("Expected modified key mapping to match with its modifier held: " + mapping.boundKey().getName());
				}
			} else {
				if (!isUnbound) {
					throw new AssertionError("Expected modified key mapping to be unbound when AMECS support is disabled.");
				}
				if (matchesWithoutModifier) {
					throw new AssertionError("Expected unsupported modified key mapping to reject input without its modifier.");
				}
				if (matchesWithModifier) {
					throw new AssertionError("Expected unsupported modified key mapping to reject input with its modifier held.");
				}
			}
			if (matchesUnknownKey) {
				throw new AssertionError("Expected modified key mapping to reject the UNKNOWN key.");
			}
		} finally {
			ClientTestUtil.runOnClient(client -> {
				platformMapping.get().setKey(InputConstants.UNKNOWN);
				KeyMapping.resetMapping();
			});
		}
	}

	private static ModifiedMapping keyboardMapping(String description, int keyCode) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.KEYSYM.getOrCreate(keyCode),
			builder -> builder.buildKeyboardKey(keyCode)
		);
	}

	private static ModifiedMapping mouseLeftMapping(String description) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT),
			IJeiKeyMappingBuilder::buildMouseLeft
		);
	}

	private static ModifiedMapping mouseRightMapping(String description) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT),
			IJeiKeyMappingBuilder::buildMouseRight
		);
	}

	private static void assertBoundFabricJeiMouseMapping(int mouseButton) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping.boundMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			JeiKeyConflictContext.GUI
		);
		IJeiKeyMappingInternal jeiMapping = new FabricJeiKeyMapping<>(fabricMapping);

		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
		if (fabricMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected the platform mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
	}

	private static void assertUnboundFabricJeiMouseMapping(int mouseButton) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping.unboundMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			JeiKeyConflictContext.GUI
		);
		IJeiKeyMappingInternal jeiMapping = new FabricJeiKeyMapping<>(fabricMapping);

		fabricMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to reject input: " + mouseKey.getName());
		}
	}

	private static void assertVanillaMouseClickIsConsumedBy(KeyMapping vanillaMapping, int mouseButton, String inputDescription) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);

		drainClicks(vanillaMapping);
		KeyMapping.click(mouseKey);

		if (!vanillaMapping.consumeClick()) {
			throw new AssertionError(
				"Expected vanilla mapping " + vanillaMapping.getName() + " to consume " + inputDescription + ": " + mouseKey.getName()
			);
		}
		if (vanillaMapping.consumeClick()) {
			throw new AssertionError(
				"Expected vanilla mapping " + vanillaMapping.getName() + " to receive exactly one click for " + inputDescription + ": " + mouseKey.getName()
			);
		}
	}

	private static void drainClicks(KeyMapping keyMapping) {
		while (keyMapping.consumeClick()) {
			// drain stale clicks from earlier setup so this assertion measures exactly one new click
		}
	}

	private static void holdShiftForKeyMappingDispatch() {
		FabricClientTestInput.holdModifier(JeiKeyModifier.SHIFT);
		KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), true);
	}

	private static void releaseShiftForKeyMappingDispatch() {
		KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), false);
		FabricClientTestInput.releaseModifier(JeiKeyModifier.SHIFT);
	}

	private interface MappingFactory {
		IJeiKeyMappingInternal build(IJeiKeyMappingBuilder builder);
	}

	private record ModifiedMapping(String description, InputConstants.Key boundKey, MappingFactory mappingFactory) {
	}

	private record TestMapping(KeyMapping platformMapping, IJeiKeyMappingInternal jeiMapping) {
	}
}
