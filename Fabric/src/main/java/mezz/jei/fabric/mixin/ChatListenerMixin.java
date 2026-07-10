package mezz.jei.fabric.mixin;

import mezz.jei.common.chat.JeiChatItemLinks;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatListenerMixin {
	@ModifyVariable(
		method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Component jei$parseJeiChatLink(Component message) {
		return JeiChatItemLinks.parseChatMessage(message)
			.orElse(message);
	}
}
