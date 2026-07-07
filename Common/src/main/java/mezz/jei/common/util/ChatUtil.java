package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Utilities for chat messages.
 */
public final class ChatUtil {
	private ChatUtil() {
	}

	public static void writeChatMessage(Player player, String translationKey, ChatFormatting color) {
		Component component = Component.translatable(translationKey)
			.withStyle(color);
		writeChatMessage(player, component);
	}

	public static void writeChatMessage(Player player, Component component) {
		player.displayClientMessage(component, false);
	}
}
