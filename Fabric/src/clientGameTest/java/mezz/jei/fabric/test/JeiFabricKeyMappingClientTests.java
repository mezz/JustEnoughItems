package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMapping;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.FabricJeiKeyMapping;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

/**
 * Verifies JEI's Fabric key-mapping wrapper on this branch.
 *
 * <p>The test creates JEI mappings through the same category builder used at startup, opens an integrated
 * world with no screen, and verifies that JEI's GUI-only mouse mappings do not prevent vanilla attack/use
 * from receiving left/right clicks, including while Shift is held.
 */
final class JeiFabricKeyMappingClientTests {
	private static final String JUNIT_SUITE_NAME = "fabric-client-key-mapping";
	private static final String TEST_NAME = "JeiFabricKeyMappingClientTests";
	private static final String CATEGORY = "key.categories.jei.test.key_mapping";
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);

	private JeiFabricKeyMappingClientTests() {

	}

	public static void register() {
		FabricClientTestRunner.register(JUNIT_SUITE_NAME, TEST_NAME, JeiFabricKeyMappingClientTests::runTest);
	}

	private static void runTest() {
		ClientTestUtil.runOnClient(client -> {
			assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.keyboardR", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R);
			assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.keyboardU", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U);
			assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.mouseLeft", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT);
			assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.mouseRight", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT);
			assertUnboundJeiKeyMappingRejectsInput();
		});
		assertModifiedJeiKeyMappings();
		assertJeiMouseMappingsDoNotHideVanillaMouseClicks();
	}

	private static void assertFabricJeiKeyMappingMatches(String description, InputConstants.Type type, int keyCode) {
		// Setup: build an unmodified Fabric JEI mapping with a test-only translation key.
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(
			CATEGORY,
			description,
			JeiKeyConflictContext.UNIVERSAL,
			JeiKeyModifier.NONE,
			type,
			keyCode
		);
		InputConstants.Key boundKey = type.getOrCreate(keyCode);

		// Operation: match the JEI wrapper against its real key and against an unbound key.
		boolean matchesBoundKey = jeiMapping.isActiveAndMatches(boundKey);
		boolean matchesUnknownKey = jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN);

		// Assertions: JEI should match its own real key while rejecting the unbound UNKNOWN key.
		if (!matchesBoundKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to match its real key: " + boundKey.getName());
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to reject the unbound UNKNOWN key.");
		}
	}

	private static void assertUnboundJeiKeyMappingRejectsInput() {
		// Setup: build an unbound JEI mapping through the category builder, the same representation used for
		// JEI controls that are intentionally not assigned to an input.
		IJeiKeyMapping jeiMapping = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
			.createMapping("key.jei.test.fabricKeyMapping.unbound")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound();
		IJeiKeyMapping registeredMapping = jeiMapping.register();

		// Operation and assertions: the unbound JEI mapping must reject vanilla mouse buttons.
		if (registeredMapping != jeiMapping) {
			throw new AssertionError("Expected Fabric JEI mapping registration to keep the custom JEI mapping instance.");
		}
		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected Fabric JEI mapping to report unbound.");
		}
		if (jeiMapping.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT))) {
			throw new AssertionError("Expected unbound Fabric JEI mapping to reject left-click input.");
		}
		if (jeiMapping.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT))) {
			throw new AssertionError("Expected unbound Fabric JEI mapping to reject right-click input.");
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
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> client.level != null && client.player != null),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for the Fabric client to load the integrated test world."
			);
			ClientTestUtil.runOnClient(client -> {
				// Setup: enter the in-game input state with no screen open and JEI's GUI context inactive.
				// JEI's GUI-only mouse mappings use the same buttons as vanilla attack/use, but vanilla
				// should remain responsible for those clicks while the player is in-world.
				client.setScreen(null);
				client.screen = null;
				if (JeiKeyConflictContext.GUI.isActive()) {
					throw new AssertionError("Expected JEI's GUI key conflict context to be inactive while testing in-world mouse input.");
				}

				assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(client.options.keyAttack, client.options.keyUse);
				assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(client.options.keyAttack, client.options.keyUse);
			});
		}
	}

	private static void assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(KeyMapping keyAttack, KeyMapping keyUse) {
		// Setup: create bound JEI mouse mappings that should be active only in JEI/GUI contexts.
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);

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
		// Setup: create unbound JEI mappings representing controls with no assigned input.
		assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
		assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);

		// Operation and assertions: unbound JEI mappings must not participate in click dispatch, so vanilla
		// attack/use still receive the click.
		assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click with unbound JEI mouse mappings");
		assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click with unbound JEI mouse mappings");
		holdShiftForKeyMappingDispatch();
		try {
			assertVanillaMouseClickIsConsumedBy(keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click with unbound JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click with unbound JEI mouse mappings");
		} finally {
			releaseShiftForKeyMappingDispatch();
		}
	}

	private static void assertModifiedMapping(
		ModifiedMapping mapping,
		JeiKeyModifier modifier
	) {
		// Setup: build the mapping through JEI's Fabric category builder so the branch's normal Fabric wrapper
		// is used.
		IJeiKeyMapping jeiMapping = ClientTestUtil.computeOnClient(client -> {
			IJeiKeyMappingBuilder builder = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
				.createMapping(mapping.description())
				.setModifier(modifier);
			return mapping.mappingFactory().build(builder);
		});

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

		// Assertions: this branch's custom Fabric wrapper supports modifiers directly.
		if (isUnbound) {
			throw new AssertionError("Expected modified Fabric JEI key mapping to stay bound.");
		}
		if (matchesWithoutModifier) {
			throw new AssertionError("Expected modified Fabric JEI key mapping to reject input when its modifier is not held.");
		}
		if (!matchesWithModifier) {
			throw new AssertionError("Expected modified Fabric JEI key mapping to match with its modifier held: " + mapping.boundKey().getName());
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected modified Fabric JEI key mapping to reject the UNKNOWN key.");
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
		IJeiKeyMappingBuilder mappingBuilder = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
			.createMapping("key.jei.test.fabricKeyMapping.boundMouse" + mouseButton)
			.setContext(JeiKeyConflictContext.GUI);
		IJeiKeyMapping jeiMapping;
		if (mouseButton == InputConstants.MOUSE_BUTTON_LEFT) {
			jeiMapping = mappingBuilder.buildMouseLeft();
		} else if (mouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
			jeiMapping = mappingBuilder.buildMouseRight();
		} else {
			throw new IllegalArgumentException("Unsupported test mouse button: " + mouseButton);
		}
		IJeiKeyMapping registeredMapping = jeiMapping.register();

		if (registeredMapping != jeiMapping) {
			throw new AssertionError("Expected bound Fabric JEI mapping registration to keep the custom JEI mapping instance: " + mouseKey.getName());
		}
		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound Fabric JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound Fabric JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
	}

	private static void assertUnboundFabricJeiMouseMapping(int mouseButton) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		IJeiKeyMapping jeiMapping = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
			.createMapping("key.jei.test.fabricKeyMapping.unboundMouse" + mouseButton)
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound();
		IJeiKeyMapping registeredMapping = jeiMapping.register();

		if (registeredMapping != jeiMapping) {
			throw new AssertionError("Expected unbound Fabric JEI mapping registration to keep the custom JEI mapping instance: " + mouseKey.getName());
		}
		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound Fabric JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound Fabric JEI mouse mapping to reject input: " + mouseKey.getName());
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
		IJeiKeyMapping build(IJeiKeyMappingBuilder builder);
	}

	private record ModifiedMapping(String description, InputConstants.Key boundKey, MappingFactory mappingFactory) {
	}
}
