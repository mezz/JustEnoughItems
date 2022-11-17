package mezz.jei.forge.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.platform.IPlatformRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RenderHelper implements IPlatformRenderHelper {
    @Override
    public Font getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
        IClientItemExtensions renderProperties = IClientItemExtensions.of(itemStack);
        Font fontRenderer = renderProperties.getFont(itemStack, IClientItemExtensions.FontContext.TOOLTIP);
        if (fontRenderer != null) {
            return fontRenderer;
        }
        return minecraft.font;
    }

    @Override
    public boolean shouldRender(MobEffectInstance potionEffect) {
        IClientMobEffectExtensions effectRenderer = IClientMobEffectExtensions.of(potionEffect);
        return effectRenderer.isVisibleInInventory(potionEffect);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(BakedModel bakedModel) {
        return bakedModel.getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public ItemColors getItemColors() {
        return Minecraft.getInstance().getItemColors();
    }

    @Override
    public Optional<NativeImage> getMainImage(TextureAtlasSprite sprite) {
        NativeImage[] frames = sprite.mainImage;
        if (frames.length == 0) {
            return Optional.empty();
        }
        NativeImage frame = frames[0];
        return Optional.ofNullable(frame);
    }

    @Override
    public void renderTooltip(
        Screen screen,
        PoseStack poseStack,
        List<Component> textComponents,
        Optional<TooltipComponent> tooltipComponent,
        int x,
        int y,
        @Nullable Font font,
        ItemStack stack
    ) {
        screen.renderTooltip(
            poseStack,
            textComponents,
            tooltipComponent,
            x,
            y,
            font,
            stack
        );
    }
}
