package mezz.jei.fabric.mixin;

import mezz.jei.common.chat.JeiChatItemLinks;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
	@ModifyArg(
		method = "handleSystemMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
		),
		index = 0
	)
	private Component jei$parseOverlayMessage(Component message) {
		return parseJeiChatLink(message);
	}

	@ModifyArg(
		method = "handleSystemMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
		),
		index = 0
	)
	private Component jei$parseSystemMessage(Component message) {
		return parseJeiChatLink(message);
	}

	@ModifyArg(
		method = "processNonPlayerChatMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
		),
		index = 0
	)
	private Component jei$parseNonPlayerChatMessage(Component message) {
		return parseJeiChatLink(message);
	}

	@ModifyArg(
		method = "showMessageToPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
			ordinal = 0
		),
		index = 0
	)
	private Component jei$parsePlayerChatMessage(Component message) {
		return parseJeiChatLink(message);
	}

	@ModifyArg(
		method = "showMessageToPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
			ordinal = 1
		),
		index = 0,
		require = 0
	)
	private Component jei$parseFilteredPlayerChatMessage(Component message) {
		return parseJeiChatLink(message);
	}

	private static Component parseJeiChatLink(Component message) {
		return JeiChatItemLinks.parseChatMessage(message)
			.orElse(message);
	}
}
