package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.AmecsHelper;
import mezz.jei.fabric.input.AmecsJeiKeyMapping;
import mezz.jei.fabric.input.AmecsKeyBindingWithContext;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

final class AmecsKeyMappingClientTestHelper {
	private AmecsKeyMappingClientTestHelper() {

	}

	public static void assertJeiKeyMappingIsDiscoverableAndRebindable(String category) {
		InputConstants.Key boundKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U);
		InputConstants.Key reboundKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Y);
		AmecsKeyBindingWithContext amecsMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecsKeyMapping.keyboardU",
			boundKey.getType(),
			boundKey.getValue(),
			category,
			new KeyModifiers(),
			JeiKeyConflictContext.UNIVERSAL
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping);
		JeiFabricKeyMappingClientTests.assertJeiKeyMappingIsDiscoverableAndRebindable(
			"AMECS",
			amecsMapping,
			jeiMapping,
			boundKey,
			reboundKey
		);
	}

	public static void assertKeyMappingConflictContexts(String category) {
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G);
		AmecsKeyBindingWithContext guiMapping = createKeyMapping(category, "conflicts.gui", key, JeiKeyConflictContext.GUI);
		AmecsKeyBindingWithContext otherGuiMapping = createKeyMapping(category, "conflicts.otherGui", key, JeiKeyConflictContext.GUI);
		AmecsKeyBindingWithContext inGameMapping = createKeyMapping(category, "conflicts.inGame", key, JeiKeyConflictContext.IN_GAME);
		AmecsKeyBindingWithContext universalMapping = createKeyMapping(category, "conflicts.universal", key, JeiKeyConflictContext.UNIVERSAL);
		KeyMapping regularMapping = new KeyMapping("key.jei.test.amecsKeyMapping.conflicts.regular", key.getType(), key.getValue(), category);

		try {
			JeiFabricKeyMappingClientTests.assertConflictContextBehavior(
				"AMECS",
				guiMapping,
				otherGuiMapping,
				inGameMapping,
				universalMapping,
				regularMapping
			);
		} finally {
			JeiFabricKeyMappingClientTests.unbindMappings(guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		}
	}

	private static AmecsKeyBindingWithContext createKeyMapping(
		String category,
		String nameSuffix,
		InputConstants.Key key,
		JeiKeyConflictContext context
	) {
		return new AmecsKeyBindingWithContext(
			"key.jei.test.amecsKeyMapping." + nameSuffix,
			key.getType(),
			key.getValue(),
			category,
			new KeyModifiers(),
			context
		);
	}

	public static void assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(String category) {
		assertVanillaMouseMappingIsNotHidden(category, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
		assertVanillaMouseMappingIsNotHidden(category, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
		assertVanillaMouseMappingIsNotHidden(category, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.CONTROL_OR_COMMAND);
		assertVanillaMouseMappingIsNotHidden(category, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.CONTROL_OR_COMMAND);
	}

	private static void assertVanillaMouseMappingIsNotHidden(String category, int mouseButton, JeiKeyModifier modifier) {
		KeyModifiers currentModifiers = KeyModifiers.getCurrentlyPressed();
		KeyModifiers previousModifiers = new KeyModifiers();
		previousModifiers.copyModifiers(currentModifiers);
		currentModifiers.unset();
		currentModifiers.set(AmecsHelper.getJeiModifier(modifier), true);

		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		AmecsKeyBindingWithContext inactiveJeiMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.inactiveJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		KeyMapping vanillaMapping = new KeyMapping(
			"key.jei.test.amecs.vanillaMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category
		);

		try {
			KeyMapping.resetMapping();
			AmecsKeyMappingManagerLayer keyMappingLayer = new AmecsKeyMappingManagerLayer();
			keyMappingLayer.register(inactiveJeiMapping);
			keyMappingLayer.register(vanillaMapping);
			List<KeyMapping> matchingMappings = keyMappingLayer.getMappingsForInput(mouseKey).toList();
			if (matchingMappings.contains(inactiveJeiMapping)) {
				throw new AssertionError("Expected AMECS to ignore inactive JEI mouse mapping: " + mouseKey.getName());
			}
			if (!matchingMappings.contains(vanillaMapping)) {
				throw new AssertionError("Expected vanilla mouse mapping to remain eligible with JEI's inactive mapping ignored: " + mouseKey.getName());
			}
		} finally {
			JeiFabricKeyMappingClientTests.unbindMappings(inactiveJeiMapping, vanillaMapping);
			currentModifiers.copyModifiers(previousModifiers);
		}
	}

	public static void assertBoundJeiMouseMapping(String category, int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		if (modifier != JeiKeyModifier.NONE) {
			jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		}
		AmecsKeyBindingWithContext amecsMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.boundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping);

		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound AMECS-backed JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound AMECS-backed JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
	}

	public static void assertUnboundJeiMouseMapping(String category, int mouseButton, JeiKeyModifier modifier) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		KeyModifiers jeiModifiers = new KeyModifiers();
		if (modifier != JeiKeyModifier.NONE) {
			jeiModifiers.set(AmecsHelper.getJeiModifier(modifier), true);
		}
		AmecsKeyBindingWithContext amecsMapping = new AmecsKeyBindingWithContext(
			"key.jei.test.amecs.unboundJeiMouse" + mouseButton + "." + modifier.name(),
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
			jeiModifiers,
			JeiKeyConflictContext.GUI
		);
		AmecsJeiKeyMapping jeiMapping = new AmecsJeiKeyMapping(amecsMapping);

		amecsMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (!KeyBindingUtils.getBoundModifiers(amecsMapping).isUnset()) {
			throw new AssertionError("Expected unbinding an AMECS-backed JEI mouse mapping to clear its modifiers: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound AMECS-backed JEI mouse mapping to reject input: " + mouseKey.getName());
		}
	}
}
