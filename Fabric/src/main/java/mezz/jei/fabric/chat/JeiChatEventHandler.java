package mezz.jei.fabric.chat;

import mezz.jei.common.chat.JeiChatItemLinks;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class JeiChatEventHandler {
	private JeiChatEventHandler() {
	}

	public static void register() {
		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			Optional<Component> parsedMessage = JeiChatItemLinks.parseChatMessage(message);
			if (parsedMessage.isEmpty()) {
				return true;
			}

			Component parsed = parsedMessage.get();
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.gui.chatListener().handleSystemMessage(parsed, false);
			return false;
		});
	}
}
