package mezz.jei.fabric.input;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyModifier;

import java.util.ArrayList;
import java.util.List;

public class AmecsHelper {
	private AmecsHelper() {}

	public static KeyModifier getJeiModifier(JeiKeyModifier modifier) {
		return switch (modifier) {
			case CONTROL_OR_COMMAND -> KeyModifier.CONTROL;
			case SHIFT -> KeyModifier.SHIFT;
			case ALT -> KeyModifier.ALT;
			case NONE -> KeyModifier.NONE;
		};
	}

	public static List<JeiKeyModifier> getJeiModifiers(KeyModifiers modifiers) {
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
