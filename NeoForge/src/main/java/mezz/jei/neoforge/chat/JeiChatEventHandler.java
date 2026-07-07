package mezz.jei.neoforge.chat;

import mezz.jei.gui.chat.JeiChatItemLinks;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

public final class JeiChatEventHandler {
    private JeiChatEventHandler() {
    }

    public static void register(PermanentEventSubscriptions subscriptions) {
        subscriptions.register(ClientChatReceivedEvent.class, JeiChatEventHandler::onChatMessageReceived);
    }

    private static void onChatMessageReceived(ClientChatReceivedEvent event) {
        String rawText = event.getMessage().getString();
        event.setMessage(JeiChatItemLinks.parse(rawText));
    }
}