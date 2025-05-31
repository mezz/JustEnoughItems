package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

/**
 * Utilities for chat messages.
 */
public final class ChatUtil {
	private ChatUtil() {
	}

	public static void writeChatMessage(Player player, String translationKey, ChatFormatting color) {
		MutableComponent component = Component.translatable(translationKey);
		Style redStyle = component.getStyle().applyFormat(color);
		component.setStyle(redStyle);
		player.sendSystemMessage(component);
	}
}
