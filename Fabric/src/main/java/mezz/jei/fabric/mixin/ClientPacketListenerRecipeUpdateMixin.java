package mezz.jei.fabric.mixin;

import mezz.jei.common.Internal;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Notifies JEI after Minecraft replaces the client's recipe container.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerRecipeUpdateMixin {
	@Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
	public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
		ClientPacketListener packetListener = (ClientPacketListener) (Object) this;
		List<Recipe<?>> recipes = List.copyOf(packetListener.getRecipeManager().getRecipes());
		Internal.setClientSyncedRecipes(recipes);
		JeiLifecycleEvents.AFTER_RECIPES_UPDATED.invoker().run();
	}
}
