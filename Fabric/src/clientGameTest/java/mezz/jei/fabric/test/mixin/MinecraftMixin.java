package mezz.jei.fabric.test.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mezz.jei.fabric.test.FabricClientTestContext;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@ModifyExpressionValue(
		method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceTime(JZ)I")
	)
	private int jei$advanceRequestedTestTicks(int ticks) {
		// Minecraft.runTick is a per-frame loop. advanceTime returns the number of
		// Minecraft.tick() calls to run this frame, often 0 between the 20 Hz ticks.
		// This handler replaces that count before Minecraft enters its tick loop.
		// While waitTicks() waits for client ticks and a completed screen render,
		// promote 0 to 1 so every frame can advance the test. Preserve larger counts
		// for catch-up, and resume normal scheduling when the wait finishes.
		// Server tick scheduling and real-time timeouts are unchanged.
		if (FabricClientTestContext.isWaitingForTicks()) {
			return Math.max(1, ticks);
		}
		return ticks;
	}
}
