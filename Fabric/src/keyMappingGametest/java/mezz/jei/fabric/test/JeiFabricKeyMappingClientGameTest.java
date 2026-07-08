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
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("UnstableApiUsage")
public class JeiFabricKeyMappingClientGameTest implements FabricClientGameTest {
	private static final String CATEGORY = "key.categories.jei.test.key_mapping";

	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReportWithBooleanVariant(
			"fabric-client-gametest",
			"jei.fabric.disableAmecsSupport",
			"without-amecs",
			getClass().getSimpleName(),
			() -> {
				context.runOnClient(client -> {
					assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.keyboardR", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R);
					assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.keyboardU", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U);
					assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.mouseLeft", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT);
					assertFabricJeiKeyMappingMatches("key.jei.test.fabricKeyMapping.mouseRight", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT);
				});
				assertModifiedJeiKeyMappings(context);
				assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(context);
			}
		);
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

		// Operation: match the JEI wrapper against the Fabric helper key and against an unbound key.
		boolean matchesBoundKey = jeiMapping.isActiveAndMatches(boundKey);
		boolean matchesUnknownKey = jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN);

		// Assertions: the Fabric helper sees the same key that JEI should match.
		if (!fabricHelperKey.equals(boundKey)) {
			throw new AssertionError("Expected Fabric's key helper to expose the test key: " + boundKey.getName());
		}
		if (!matchesBoundKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to match its Fabric helper key: " + boundKey.getName());
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to reject the unbound UNKNOWN key.");
		}
	}

	private static void assertModifiedJeiKeyMappings(ClientGameTestContext context) {
		assertModifiedMapping(context, keyboardMapping("key.jei.test.modifiedKeyMapping.shiftKeyboardR", GLFW.GLFW_KEY_R), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, keyboardMapping("key.jei.test.modifiedKeyMapping.controlOrCommandKeyboardU", GLFW.GLFW_KEY_U), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseLeftMapping("key.jei.test.modifiedKeyMapping.shiftMouseLeft"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.shiftMouseRight"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, mouseLeftMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseLeft"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseRight"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.altMouseRight"), JeiKeyModifier.ALT);
	}

	private static void assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(ClientGameTestContext context) {
		if (!FabricAmecsSupport.isEnabled()) {
			return;
		}

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getClientWorld().waitForChunksRender();
			context.runOnClient(client -> {
				// Setup: reproduce the AMECS issue in a world with no screen open. JEI has GUI-only modified
				// mouse bindings, while vanilla has normal unmodified attack/use mouse bindings.
				if (client.screen != null || JeiKeyConflictContext.GUI.isActive()) {
					throw new AssertionError("Expected no screen to be open while testing in-game mouse input.");
				}

				// Operation and assertions: JEI's inactive GUI-only AMECS mappings must not consume the mouse key
				// or conflict with a vanilla unmodified mouse binding while the player is in-game.
				assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
				assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
				assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND);
				assertVanillaMouseMappingIsNotHidden(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND);
			});
		}
	}

	private static void assertModifiedMapping(
		ClientGameTestContext context,
		ModifiedMapping mapping,
		JeiKeyModifier modifier
	) {
		// Setup: build the mapping through JEI's Fabric category builder so AMECS support is selected once for
		// this client startup, the same way it is during normal JEI initialization.
		IJeiKeyMappingInternal jeiMapping = context.computeOnClient(client -> {
			IJeiKeyMappingBuilder builder = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
				.createMapping(mapping.description())
				.setModifier(modifier);
			return mapping.mappingFactory().build(builder);
		});

		// Operation: ask JEI whether the mapping accepts the real input key before and after holding the modifier.
		boolean isUnbound = context.computeOnClient(client -> jeiMapping.isUnbound());
		boolean matchesWithoutModifier = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
		holdModifier(context.getInput(), modifier);
		boolean matchesWithModifier;
		boolean matchesUnknownKey;
		try {
			matchesWithModifier = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
			matchesUnknownKey = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN));
		} finally {
			releaseModifier(context.getInput(), modifier);
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
		AmecsKeyBindingWithContext inactiveJeiMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.inactiveJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		KeyMapping vanillaMapping = new KeyMapping(
			"key.jei.test.amecs.vanillaMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(inactiveJeiMapping, JeiKeyConflictContext.GUI);

		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected JEI's inactive GUI mouse mapping to reject in-game input: " + mouseKey.getName());
		}
		if (inactiveJeiMapping.same(vanillaMapping)) {
			throw new AssertionError("Expected JEI's inactive AMECS mouse mapping to avoid conflicting with vanilla: " + mouseKey.getName());
		}
		if (!vanillaMapping.matchesMouse(mouseButton)) {
			throw new AssertionError("Expected vanilla mouse mapping to remain eligible with JEI's inactive mapping ignored: " + mouseKey.getName());
		}
	}

	private static void holdModifier(TestInput input, JeiKeyModifier modifier) {
		switch (modifier) {
			case CONTROL_OR_COMMAND -> input.holdControl();
			case SHIFT -> input.holdShift();
			case ALT -> input.holdAlt();
			default -> throw new IllegalArgumentException("Unsupported test modifier: " + modifier);
		}
	}

	private static void releaseModifier(TestInput input, JeiKeyModifier modifier) {
		switch (modifier) {
			case CONTROL_OR_COMMAND -> input.releaseControl();
			case SHIFT -> input.releaseShift();
			case ALT -> input.releaseAlt();
			default -> throw new IllegalArgumentException("Unsupported test modifier: " + modifier);
		}
	}

	private interface MappingFactory {
		IJeiKeyMappingInternal build(IJeiKeyMappingBuilder builder);
	}

	private record ModifiedMapping(String description, InputConstants.Key boundKey, MappingFactory mappingFactory) {
	}
}
