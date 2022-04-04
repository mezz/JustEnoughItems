package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IPlatformRenderHelper {
    Font getFontRenderer(Minecraft minecraft, ItemStack itemStack);

    boolean shouldRender(MobEffectInstance potionEffect);

    TextureAtlasSprite getParticleIcon(BakedModel bakedModel);

    ItemColors getItemColors();

    @Nullable
    NativeImage getMainImage(TextureAtlasSprite sprite);
}
