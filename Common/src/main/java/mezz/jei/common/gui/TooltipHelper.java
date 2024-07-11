package mezz.jei.common.gui;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class TooltipHelper {
	public static ClientTooltipComponent createKeyUsageTooltipComponent(String translationKey, IJeiKeyMapping keyMapping) {
		return toTooltipComponent(createKeyUsageComponent(translationKey, keyMapping));
	}

	public static ClientTooltipComponent createKeyUsageTooltipComponent(String translationKey, MutableComponent keyMapping) {
		return toTooltipComponent(createKeyUsageComponent(translationKey, keyMapping));
	}

	public static Component createKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping) {
		MutableComponent translatedKeyMessage = keyMapping.getTranslatedKeyMessage().copy();
		return createKeyUsageComponent(translationKey, translatedKeyMessage);
	}

	public static Component createKeyUsageComponent(String translationKey, MutableComponent keyMapping) {
		Component boldKeyMapping = keyMapping.withStyle(ChatFormatting.BOLD);
		return Component.translatable(translationKey, boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);
	}

	public static ClientTooltipComponent toTooltipComponent(Component component) {
		return ClientTooltipComponent.create(component.getVisualOrderText());
	}

	public static List<ClientTooltipComponent> toTooltipComponents(List<Component> components) {
		List<ClientTooltipComponent> tooltipComponents = new ArrayList<>(components.size());
		for (Component component : components) {
			tooltipComponents.add(toTooltipComponent(component));
		}
		return tooltipComponents;
	}

	/**
	 * The vanilla translation for left click is "LEFT BUTTON" and right click is "RIGHT BUTTON".
	 * We want better names for these in tooltips, and so use our own localization.
	 */
	public static Component getKeyDisplayName(InputConstants.Key key) {
		// The vanilla translation for left click is "LEFT BUTTON" and right click is "RIGHT BUTTON".
		// We want better names for these in tooltips, and so use our own localization.
		if (key.getType() == InputConstants.Type.MOUSE) {
			int value = key.getValue();
			if (value == InputConstants.MOUSE_BUTTON_LEFT) {
				return Component.translatable("jei.key.mouse.left");
			} else if (value == InputConstants.MOUSE_BUTTON_RIGHT) {
				return Component.translatable("jei.key.mouse.right");
			}
		}
		return key.getDisplayName();
	}
}
