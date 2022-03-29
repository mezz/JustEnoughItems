package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side-safe utilities for chat messages.
 */
public final class ChatUtilServer {
	private ChatUtilServer() {
	}

	public static void writeChatMessage(Player player, String translationKey, ChatFormatting color) {
		TranslatableComponent component = new TranslatableComponent(translationKey);
		component.getStyle().applyFormat(color);
		player.sendMessage(component, Util.NIL_UUID);
	}
}
