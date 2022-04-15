package mezz.jei.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccess {
    @Accessor
    ItemColors getItemColors();

    @Accessor
    TextureManager getTextureManager();
}
