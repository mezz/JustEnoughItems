package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.AmecsHelper;
import mezz.jei.fabric.input.AmecsJeiKeyMapping;
import mezz.jei.fabric.input.AmecsJeiKeyMappingManagerLayer;
import mezz.jei.fabric.input.AmecsKeyMappingWithContext;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.fabric.input.FabricJeiKeyMapping;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Verifies JEI's Fabric key-mapping wrappers with and without AMECS support.
 *
 * <p>This test class is run by two Fabric client launches:
 * <ul>
 *     <li>{@code :Fabric:runClientGameTest} leaves JEI's AMECS support enabled.</li>
 *     <li>{@code :Fabric:runClientGameTestWithoutAmecs} sets {@code -Djei.fabric.disableAmecsSupport=true},
 *     so JEI uses its non-AMECS Fabric key mapping path even though the test AMECS dependency is still present.</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricKeyMappingClientGameTest implements FabricClientGameTest {
	private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("jei-test", "key_mapping"));

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
				assertJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			}
		);
	}

	private static void assertFabricJeiKeyMappingMatches(String description, InputConstants.Type type, int keyCode) {
		// Setup: FabricKeyMapping stores the real key separately, then clears the vanilla key field so the
		// binding does not conflict with other mods' keybinds.
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			description,
			type,
			keyCode,
			CATEGORY,
			JeiKeyConflictContext.UNIVERSAL
		);
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.UNIVERSAL);
		InputConstants.Key boundKey = type.getOrCreate(keyCode);
		InputConstants.Key hiddenVanillaKey = KeyMappingHelper.getBoundKeyOf(fabricMapping);

		// Operation: match the JEI wrapper against the real input key and against the hidden vanilla key.
		boolean matchesBoundKey = jeiMapping.isActiveAndMatches(boundKey);
		boolean matchesUnknownKey = jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN);

		// Assertions: Fabric still sees this binding as unbound, but JEI must match the preserved real key.
		if (!hiddenVanillaKey.equals(InputConstants.UNKNOWN)) {
			throw new AssertionError("Expected FabricKeyMapping to hide its vanilla bound key from Fabric's key helper.");
		}
		if (!matchesBoundKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to match its stored real key: " + boundKey.getName());
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected FabricJeiKeyMapping to reject the hidden vanilla UNKNOWN key.");
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

	private static void assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings() {
		if (!FabricAmecsSupport.isEnabled()) {
			return;
		}

		// Setup: AMECS-backed JEI mappings can use modifiers on the same mouse buttons as vanilla attack/use.
		// Operation and assertions: when JEI's GUI context is inactive, these mappings must not consume the
		// modified mouse input or conflict with vanilla's unmodified mouse bindings.
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND));
	}

	private static void assertJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getClientLevel().waitForChunksRender();
			context.runOnClient(client -> client.gui.setScreen(null));
			context.waitFor(
				client -> client.level != null &&
					client.gui.screen() == null &&
					!JeiKeyConflictContext.GUI.isActive(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			// Setup: enter the in-game input state with no screen open and JEI's GUI context inactive.
			// JEI's GUI-only mouse mappings use the same buttons as vanilla attack/use, but vanilla
			// should remain responsible for those clicks while the player is in-world.
			assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			context.runOnClient(client -> assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings());
		}
	}

	private static void assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		context.runOnClient(client -> {
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
			assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click with bound JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click with bound JEI mouse mappings");
		});
		holdShiftForKeyMappingDispatch(context);
		try {
			context.runOnClient(client -> {
				assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click with bound JEI mouse mappings");
				assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click with bound JEI mouse mappings");
			});
		} finally {
			releaseShiftForKeyMappingDispatch(context);
		}
	}

	private static void assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		context.runOnClient(client -> {
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
			assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click after unbinding JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click after unbinding JEI mouse mappings");
		});
		holdShiftForKeyMappingDispatch(context);
		try {
			context.runOnClient(client -> {
				assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click after unbinding JEI mouse mappings");
				assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click after unbinding JEI mouse mappings");
			});
		} finally {
			releaseShiftForKeyMappingDispatch(context);
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

	private static MouseMappingMatches getMouseMappingMatches(int mouseButton, JeiKeyModifier modifier) {
		AmecsKeyModifierCombination previousModifiers = new AmecsKeyModifierCombination();
		AmecsKeyModifierCombination currentModifiers = AmecsKeyModifierCombination.getCurrentlyPressed();
		previousModifiers.copyFrom(currentModifiers);

		AmecsKeyModifierCombination pressedModifiers = new AmecsKeyModifierCombination();
		AmecsHelper.setJeiModifier(pressedModifiers, modifier);
		currentModifiers.copyFrom(pressedModifiers);
		try {
			InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
			AmecsJeiKeyMappingManagerLayer jeiLayer = new AmecsJeiKeyMappingManagerLayer();
			AmecsKeyMappingManagerLayer vanillaLayer = new AmecsKeyMappingManagerLayer();

			AmecsKeyModifierCombination jeiModifiers = new AmecsKeyModifierCombination();
			AmecsHelper.setJeiModifier(jeiModifiers, modifier);
			KeyMapping inactiveJeiMapping = new AmecsKeyMappingWithContext(
				"key.jei.test.amecs.inactiveJeiMouse" + mouseButton,
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

			jeiLayer.register(inactiveJeiMapping);
			vanillaLayer.register(vanillaMapping);
			List<KeyMapping> matchingJeiMappings = jeiLayer.getMappingsForInput(mouseKey).toList();
			List<KeyMapping> matchingVanillaMappings = vanillaLayer.getMappingsForInput(mouseKey).toList();
			return new MouseMappingMatches(mouseKey, vanillaMapping, matchingJeiMappings, matchingVanillaMappings);
		} finally {
			currentModifiers.copyFrom(previousModifiers);
		}
	}

	private static void assertVanillaMouseMappingIsNotHidden(MouseMappingMatches matches) {
		if (!matches.jeiMappings().isEmpty()) {
			throw new AssertionError("Expected AMECS to ignore inactive JEI mouse mapping: " + matches.mouseKey().getName());
		}
		if (!matches.vanillaMappings().contains(matches.vanillaMapping())) {
			throw new AssertionError("Expected vanilla mouse mapping to remain eligible with JEI's inactive mapping ignored: " + matches.mouseKey().getName());
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
		AmecsKeyModifierCombination jeiModifiers = new AmecsKeyModifierCombination();
		AmecsHelper.setJeiModifier(jeiModifiers, modifier);
		AmecsKeyMappingWithContext amecsMapping = new AmecsKeyMappingWithContext(
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
		AmecsKeyModifierCombination jeiModifiers = new AmecsKeyModifierCombination();
		AmecsHelper.setJeiModifier(jeiModifiers, modifier);
		AmecsKeyMappingWithContext amecsMapping = new AmecsKeyMappingWithContext(
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
			// Drain stale clicks from earlier setup so this assertion measures exactly one new click.
		}
	}

	private static void holdShiftForKeyMappingDispatch(ClientGameTestContext context) {
		context.getInput().holdShift();
		context.runOnClient(client -> KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), true));
	}

	private static void releaseShiftForKeyMappingDispatch(ClientGameTestContext context) {
		context.runOnClient(client -> KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), false));
		context.getInput().releaseShift();
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

	private record MouseMappingMatches(
		InputConstants.Key mouseKey,
		KeyMapping vanillaMapping,
		List<KeyMapping> jeiMappings,
		List<KeyMapping> vanillaMappings
	) {
	}
}
