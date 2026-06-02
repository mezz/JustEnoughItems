package mezz.jei.fabric.input;

import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifier;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.input.InputQuirks;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AmecsHelper {
	public static AmecsKeyModifier COMMAND = new AmecsJeiKeyModifier("jei.key.combo.command", null, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);

	public static void init() {
		AmecsKeyModifiers.register(COMMAND);
	}

	private AmecsHelper() {}

	public static void setJeiModifier(AmecsKeyModifierCombination combination, JeiKeyModifier modifier) {
		switch (modifier) {
			case CONTROL -> combination.set(AmecsKeyModifiers.CONTROL, true);
			case CONTROL_OR_COMMAND -> {
				if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
					combination.set(AmecsHelper.COMMAND, true);
				} else {
					combination.set(AmecsKeyModifiers.CONTROL, true);
				}
			}
			case SHIFT -> combination.set(AmecsKeyModifiers.SHIFT, true);
			case ALT -> combination.set(AmecsKeyModifiers.ALT, true);
			case NONE -> combination.unset();
		}
	}

	public static List<JeiKeyModifier> getJeiModifiers(AmecsKeyModifierCombination modifiers) {
		if (modifiers.isUnset()) {
			return List.of(JeiKeyModifier.NONE);
		}
		List<JeiKeyModifier> modifiersList = new ArrayList<>();
		if (modifiers.getAlt()) {
			modifiersList.add(JeiKeyModifier.ALT);
		}
		if (modifiers.getControl()) {
			modifiersList.add(JeiKeyModifier.CONTROL_OR_COMMAND);
		}
		if (modifiers.getShift()) {
			modifiersList.add(JeiKeyModifier.SHIFT);
		}
		return modifiersList;
	}
}
