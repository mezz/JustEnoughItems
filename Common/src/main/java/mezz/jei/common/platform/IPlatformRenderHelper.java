package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface IPlatformRenderHelper {
	Font getFontRenderer(Minecraft minecraft, ItemStack itemStack);

	boolean shouldRender(MobEffectInstance potionEffect);

	TextureAtlasSprite getParticleIcon(BakedModel bakedModel);

	ItemColors getItemColors();

	Optional<NativeImage> getMainImage(TextureAtlasSprite sprite);

	void renderTooltip(GuiGraphics guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack);
}
