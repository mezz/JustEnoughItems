package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.AmecsHelper;
import mezz.jei.fabric.input.AmecsJeiKeyMapping;
import mezz.jei.fabric.input.AmecsJeiKeyMappingManagerLayer;
import mezz.jei.fabric.input.AmecsKeyMappingWithContext;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

final class AmecsKeyMappingClientGameTestHelper {
	private AmecsKeyMappingClientGameTestHelper() {

	}

	public static void assertJeiKeyMappingIsDiscoverableAndRebindable(KeyMapping.Category category) {
		InputConstants.Key boundKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U);
		InputConstants.Key reboundKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Y);
		AmecsKeyMappingWithContext amecsMapping = new AmecsKeyMappingWithContext(
			"key.jei.test.amecsKeyMapping.keyboardU",
			boundKey.getType(),
			boundKey.getValue(),
			category,
			new AmecsKeyModifierCombination(),
			JeiKeyConflictContext.UNIVERSAL
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping, JeiKeyConflictContext.UNIVERSAL);
		JeiFabricKeyMappingClientGameTest.assertJeiKeyMappingIsDiscoverableAndRebindable(
			"AMECS",
			amecsMapping,
			jeiMapping,
			boundKey,
			reboundKey
		);
	}

	public static void assertKeyMappingConflictContexts(KeyMapping.Category category) {
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G);
		AmecsKeyMappingWithContext guiMapping = createKeyMapping(category, "conflicts.gui", key, JeiKeyConflictContext.GUI);
		AmecsKeyMappingWithContext otherGuiMapping = createKeyMapping(category, "conflicts.otherGui", key, JeiKeyConflictContext.GUI);
		AmecsKeyMappingWithContext inGameMapping = createKeyMapping(category, "conflicts.inGame", key, JeiKeyConflictContext.IN_GAME);
		AmecsKeyMappingWithContext universalMapping = createKeyMapping(category, "conflicts.universal", key, JeiKeyConflictContext.UNIVERSAL);
		KeyMapping regularMapping = new KeyMapping("key.jei.test.amecsKeyMapping.conflicts.regular", key.getType(), key.getValue(), category);

		try {
			JeiFabricKeyMappingClientGameTest.assertConflictContextBehavior(
				"AMECS",
				guiMapping,
				otherGuiMapping,
				inGameMapping,
				universalMapping,
				regularMapping
			);
		} finally {
			JeiFabricKeyMappingClientGameTest.unbindMappings(guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		}
	}

	private static AmecsKeyMappingWithContext createKeyMapping(
		KeyMapping.Category category,
		String nameSuffix,
		InputConstants.Key key,
		JeiKeyConflictContext context
	) {
		return new AmecsKeyMappingWithContext(
			"key.jei.test.amecsKeyMapping." + nameSuffix,
			key.getType(),
			key.getValue(),
			category,
			new AmecsKeyModifierCombination(),
			context
		);
	}

	public static void assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(KeyMapping.Category category) {
		// Setup: AMECS-backed JEI mappings can use modifiers on the same mouse buttons as vanilla attack/use.
		// Operation and assertions: when JEI's GUI context is inactive, these mappings must not consume the
		// modified mouse input or conflict with vanilla's unmodified mouse bindings.
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(category, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(category, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(category, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND));
		assertVanillaMouseMappingIsNotHidden(getMouseMappingMatches(category, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND));
	}

	public static void assertBoundJeiMouseMapping(KeyMapping.Category category, int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		AmecsKeyModifierCombination jeiModifiers = new AmecsKeyModifierCombination();
		AmecsHelper.setJeiModifier(jeiModifiers, modifier);
		AmecsKeyMappingWithContext amecsMapping = new AmecsKeyMappingWithContext(
			"key.jei.test.amecs.boundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
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

	public static void assertUnboundJeiMouseMapping(KeyMapping.Category category, int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		AmecsKeyModifierCombination jeiModifiers = new AmecsKeyModifierCombination();
		AmecsHelper.setJeiModifier(jeiModifiers, modifier);
		AmecsKeyMappingWithContext amecsMapping = new AmecsKeyMappingWithContext(
			"key.jei.test.amecs.unboundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping, JeiKeyConflictContext.GUI);

		amecsMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (!AmecsKeyModifiersApi.getBoundModifiers(amecsMapping).isUnset()) {
			throw new AssertionError("Expected unbinding an AMECS-backed JEI mouse mapping to clear its modifiers: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to reject input: " + mouseKey.getName());
		}
	}

	private static MouseMappingMatches getMouseMappingMatches(KeyMapping.Category category, int mouseButton, JeiKeyModifier modifier) {
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
				category,
				jeiModifiers,
				JeiKeyConflictContext.GUI
			);
			KeyMapping vanillaMapping = new KeyMapping(
				"key.jei.test.amecs.vanillaMouse" + mouseButton,
				InputConstants.Type.MOUSE,
				mouseButton,
				category
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

	private record MouseMappingMatches(
		InputConstants.Key mouseKey,
		KeyMapping vanillaMapping,
		List<KeyMapping> jeiMappings,
		List<KeyMapping> vanillaMappings
	) {
	}
}
