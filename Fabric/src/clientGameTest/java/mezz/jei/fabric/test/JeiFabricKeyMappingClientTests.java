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
		assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings();
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

		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.runOnClient(client -> client.setScreen(null));
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> {
					// Setup: reproduce the AMECS issue in a world with no screen open. JEI has GUI-only modified
					// mouse bindings, while vanilla has normal unmodified attack/use mouse bindings.
					if (client.level == null) {
						return false;
					}
					if (client.screen != null || JeiKeyConflictContext.GUI.isActive()) {
						client.setScreen(null);
						return false;
					}

					// Operation and assertions: JEI's inactive AMECS mappings must not consume the mouse key or conflict
					// with a vanilla unmodified mouse binding.
					assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
					assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
					assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND);
					assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND);
					return true;
				}),
				ASSERTION_TIMEOUT,
				() -> "Timed out waiting for the Fabric client to enter the in-game input state."
			);
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

	private interface MappingFactory {
		IJeiKeyMappingInternal build(IJeiKeyMappingBuilder builder);
	}

	private record ModifiedMapping(String description, InputConstants.Key boundKey, MappingFactory mappingFactory) {
	}
}
