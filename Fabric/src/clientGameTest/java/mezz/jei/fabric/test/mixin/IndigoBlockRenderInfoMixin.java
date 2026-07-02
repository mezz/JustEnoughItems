package mezz.jei.fabric.test.mixin;

import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Releases Fabric 0.77's cached chunk region between disposable test worlds. */
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo", remap = false)
abstract class IndigoBlockRenderInfoMixin {
	@Shadow
	@Nullable
	BlockAndTintGetter blockView;

	@Inject(
		method = "release",
		at = @At("RETURN")
	)
	private void releaseChunkRegion(CallbackInfo ci) {
		this.blockView = null;
	}
}
