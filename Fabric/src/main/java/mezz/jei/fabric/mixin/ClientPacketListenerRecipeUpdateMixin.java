package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notifies JEI after Minecraft replaces the client's recipe container.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerRecipeUpdateMixin {
	@Inject(
		method = "handleUpdateRecipes",
		at = @At("RETURN")
	)
	public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
		JeiLifecycleEvents.AFTER_RECIPES_UPDATED.invoker().run();
	}
}
