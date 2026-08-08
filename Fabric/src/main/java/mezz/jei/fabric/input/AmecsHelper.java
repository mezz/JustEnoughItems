package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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

	public static Component getCombinedName(KeyModifiers modifiers, InputConstants.Key key) {
		Component component = key.getDisplayName();
		for (JeiKeyModifier modifier : getJeiModifiers(modifiers)) {
			component = switch (modifier) {
				case CONTROL_OR_COMMAND -> new TranslatableComponent(
					Minecraft.ON_OSX ? "jei.key.combo.command" : "jei.key.combo.control",
					component
				);
				case SHIFT -> new TranslatableComponent("jei.key.combo.shift", component);
				case ALT -> new TranslatableComponent("jei.key.combo.alt", component);
				case NONE -> component;
			};
		}
		return component;
	}
}
