package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Utilities for chat messages.
 */
public final class ChatUtil {
	private ChatUtil() {
	}

	public static void writeChatMessage(LocalPlayer player, String translationKey, ChatFormatting color) {
		TranslatableComponent component = new TranslatableComponent(translationKey);
		component.getStyle().applyFormat(color);
		player.sendMessage(component, Util.NIL_UUID);
	}
}
