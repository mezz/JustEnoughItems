package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Utilities for chat messages.
 */
public final class ChatUtil {
	private ChatUtil() {
	}

	public static void writeChatMessage(LocalPlayer player, String translationKey, ChatFormatting color) {
		Component component = Component.translatable(translationKey);
		component.getStyle().applyFormat(color);
		player.sendSystemMessage(component);
	}
}
