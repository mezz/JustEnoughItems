package mezz.jei.fabric.chat;

import mezz.jei.gui.chat.JeiChatItemLinks;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class JeiChatEventHandler {
    private JeiChatEventHandler() {
    }

    public static void register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String rawText = message.getString();
            if (!rawText.contains("[JEI:")) {
                return true;
            }

            Component parsed = JeiChatItemLinks.parse(rawText);
            Minecraft.getInstance().gui.chatListener().handleSystemMessage(parsed, false);
            return false;
        });
    }
}