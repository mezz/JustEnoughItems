package mezz.jei.neoforge.chat;

import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.Optional;

public final class JeiChatEventHandler {
	private JeiChatEventHandler() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(ClientChatReceivedEvent.class, JeiChatEventHandler::onChatMessageReceived);
	}

	private static void onChatMessageReceived(ClientChatReceivedEvent event) {
		Optional<Component> parsedMessage = JeiChatItemLinks.parseChatMessage(event.getMessage());
		if (parsedMessage.isEmpty()) {
			return;
		}

		Component parsed = parsedMessage.get();
		event.setMessage(parsed);
	}
}
