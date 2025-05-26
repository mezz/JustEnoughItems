package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IPlatformRenderHelper {
	Font getFontRenderer(Minecraft minecraft, ItemStack itemStack);

	boolean shouldRender(MobEffectInstance potionEffect);

	Optional<NativeImage> getMainImage(TextureAtlasSprite sprite);

	void renderTooltip(GuiGraphics guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack);

	Component getName(TagKey<?> tagKey);

	MultiBufferSource getBufferSource(GuiGraphics guiGraphics);

	@Nullable
	TextureAtlasSprite getTextureAtlasSprite(BlockState blockState);
}
