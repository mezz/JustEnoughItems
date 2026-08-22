package mezz.jei.fabric.mixin;

import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	@Shadow
	private @Nullable GuiItemAtlas itemAtlas;

	@Inject(
		method = "prepareItemAtlas",
		at = @At("HEAD")
	)
	private void shrinkItemAtlas(Set<Object> itemsInFrame, int slotTextureSize, CallbackInfoReturnable<GuiItemAtlas> cir) {
		if (this.itemAtlas == null) {
			return;
		}

		int requiredTextureSize = GuiItemAtlas.computeTextureSizeFor(slotTextureSize, itemsInFrame.size());
		if (requiredTextureSize < this.itemAtlas.textureSize()) {
			this.itemAtlas.close();
			this.itemAtlas = null;
		}
	}
}
