package mezz.jei.fabric.mixin;

import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        JeiLifecycleEvents.AFTER_RECIPE_SYNC.invoker().run();
    }

    @Inject(
        method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V",
            shift = At.Shift.AFTER,
            ordinal = 0
        )
    )
    public void handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        JeiLifecycleEvents.GAME_START.invoker().run();
    }
}
