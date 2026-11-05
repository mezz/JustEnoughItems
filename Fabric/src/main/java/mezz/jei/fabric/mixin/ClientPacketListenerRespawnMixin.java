package mezz.jei.fabric.mixin;

import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Releases fake-menu inventories after a respawn or dimension change replaces the client player.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerRespawnMixin {
	@Inject(
		method = "handleRespawn",
		at = @At("RETURN")
	)
	public void handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		VanillaPlugin.clearPlayerDependentCaches();
	}
}
