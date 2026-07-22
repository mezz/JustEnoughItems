package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.AmecsHelper;
import mezz.jei.fabric.input.AmecsJeiKeyMapping;
import mezz.jei.fabric.input.AmecsKeyBindingWithContext;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.fabric.input.FabricJeiKeyMapping;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import mezz.jei.fabric.input.FabricKeyMapping;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

/**
 * Verifies JEI's Fabric key-mapping wrappers with and without AMECS support.
 *
 * <p>This test class is run by two Fabric client launches:
 * <ul>
 *     <li>{@code :Fabric:runClientKeyMappingTest} leaves JEI's AMECS support enabled.</li>
 *     <li>{@code :Fabric:runClientKeyMappingTestWithoutAmecs} sets {@code -Djei.fabric.disableAmecsSupport=true},
 *     so JEI uses its non-AMECS Fabric key mapping path even though the test AMECS dependency is still present.</li>
 * </ul>
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
		});
		assertModifiedJeiKeyMappings();
		assertJeiMouseMappingsDoNotHideVanillaMouseClicks();
	}

	private static void assertFabricJeiKeyMappingMatches(String description, InputConstants.Type type, int keyCode) {
		// Setup: build an unmodified Fabric-backed JEI mapping with a test-only translation key.
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			description,
			type,
			keyCode,
			CATEGORY,
			JeiKeyConflictContext.UNIVERSAL
		);
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.UNIVERSAL);
		InputConstants.Key boundKey = type.getOrCreate(keyCode);
		InputConstants.Key fabricHelperKey = KeyBindingHelper.getBoundKeyOf(fabricMapping);

		// Operation: match the JEI wrapper against its real key and against an unbound key.
		boolean fabricMappingMatchesKey = switch (type) {
			case KEYSYM -> fabricMapping.matches(keyCode, InputConstants.UNKNOWN.getValue());
			case MOUSE -> fabricMapping.matchesMouse(keyCode);
			case SCANCODE -> fabricMapping.matches(InputConstants.UNKNOWN.getValue(), keyCode);
		};
		boolean matchesBoundKey = jeiMapping.isActiveAndMatches(boundKey);
		boolean matchesUnknownKey = jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN);

		// Assertions: the Fabric helper sees the intentionally hidden parent key, while JEI still matches
		// FabricKeyMapping's real key.
		if (!fabricHelperKey.equals(InputConstants.UNKNOWN)) {
			throw new AssertionError("Expected Fabric's key helper to see FabricKeyMapping as unbound: " + boundKey.getName());
		}
		if (!fabricMappingMatchesKey) {
			throw new AssertionError("Expected FabricKeyMapping to match its real key: " + boundKey.getName());
		}
		if (!fabricMapping.saveString().equals(boundKey.getName())) {
			throw new AssertionError("Expected FabricKeyMapping to save its real key: " + boundKey.getName());
		}
		if (!matchesBoundKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to match its real key: " + boundKey.getName());
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to reject the unbound UNKNOWN key.");
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

	private static void assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings() {
		if (!FabricAmecsSupport.isEnabled()) {
			return;
		}

		// Setup: AMECS-backed JEI mappings can use modifiers on the same mouse buttons as vanilla attack/use.
		// Operation and assertions: when JEI's GUI context is inactive, these mappings must not consume the
		// modified mouse input or conflict with vanilla's unmodified mouse bindings.
		assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
		assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
		assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND);
		assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND);
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
				assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings();
			});
		}
	}

	private static void assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(KeyMapping keyAttack, KeyMapping keyUse) {
		// Setup: create bound JEI mouse mappings that should be active only in JEI/GUI contexts.
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
		assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);
		if (FabricAmecsSupport.isEnabled()) {
			assertBoundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
			assertBoundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
			assertBoundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
			assertBoundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
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
			assertUnboundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
			assertUnboundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
			assertUnboundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
			assertUnboundAmecsJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
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
		IJeiKeyMappingInternal jeiMapping = ClientTestUtil.computeOnClient(client -> {
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

		// Assertions: when AMECS support is enabled, the modified mapping stays bound and only matches with
		// its modifier held. When AMECS support is disabled, the modified mapping is unsupported and unbound.
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

	private static void assertVanillaMouseMappingIsNotHidden(int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		JeiKeyConflictContext inactiveContext = JeiKeyConflictContext.GUI;
		AmecsKeyBindingWithContext inactiveJeiMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.inactiveJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			jeiModifiers,
			inactiveContext
		);
		KeyMapping vanillaMapping = new KeyMapping(
			"key.jei.test.amecs.vanillaMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(inactiveJeiMapping, inactiveContext);

		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected JEI's inactive AMECS mouse mapping to reject input: " + mouseKey.getName());
		}
		if (inactiveJeiMapping.same(vanillaMapping)) {
			throw new AssertionError("Expected JEI's inactive AMECS mouse mapping to avoid conflicting with vanilla: " + mouseKey.getName());
		}
		if (!vanillaMapping.matchesMouse(mouseButton)) {
			throw new AssertionError("Expected vanilla mouse mapping to remain eligible with JEI's inactive mapping ignored: " + mouseKey.getName());
		}
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
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.GUI);

		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
	}

	private static void assertBoundAmecsJeiMouseMapping(int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		if (modifier != JeiKeyModifier.NONE) {
			jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		}
		AmecsKeyBindingWithContext amecsMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.boundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping, JeiKeyConflictContext.GUI);

		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound AMECS-backed JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound AMECS-backed JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
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
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.GUI);

		fabricMapping.setKey(InputConstants.UNKNOWN);

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to reject input: " + mouseKey.getName());
		}
	}

	private static void assertUnboundAmecsJeiMouseMapping(int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		if (modifier != JeiKeyModifier.NONE) {
			jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		}
		AmecsKeyBindingWithContext amecsMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.unboundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping, JeiKeyConflictContext.GUI);

		amecsMapping.setKey(InputConstants.UNKNOWN);

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to reject input: " + mouseKey.getName());
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
}
