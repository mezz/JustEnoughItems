package mezz.jei.fabric.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RenderHelper implements IPlatformRenderHelper {
	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		return minecraft.font;
	}

	@Override
	public boolean shouldRender(MobEffectInstance potionEffect) {
		return true;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getTextureAtlasSprite(BlockState blockState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockRenderDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();
		BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
		TextureAtlasSprite textureAtlasSprite = blockModelShapes.getParticleIcon(blockState);
		if (textureAtlasSprite.atlasLocation().equals(MissingTextureAtlasSprite.getLocation())) {
			return null;
		}
		return textureAtlasSprite;
	}

	@Override
	public Optional<NativeImage> getMainImage(TextureAtlasSprite sprite) {
		@SuppressWarnings("resource")
		SpriteContents contents = sprite.contents();
		NativeImage[] frames = contents.byMipLevel;
		if (frames.length == 0) {
			return Optional.empty();
		}
		NativeImage frame = frames[0];
		return Optional.ofNullable(frame);
	}

	@Override
	public void renderTooltip(GuiGraphics guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack) {
		List<ClientTooltipComponent> components = elements.stream()
			.flatMap(e -> e.map(
				text -> font.split(text, 400).stream().map(ClientTooltipComponent::create),
				tooltipComponent -> Stream.of(createClientTooltipComponent(tooltipComponent))
			))
			.collect(Collectors.toCollection(ArrayList::new));

		guiGraphics.renderTooltipInternal(font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
	}

	@Override
	public Component getName(TagKey<?> tagKey) {
		return tagKey.getName();
	}

	@Override
	public MultiBufferSource getBufferSource(GuiGraphics guiGraphics) {
		return guiGraphics.bufferSource;
	}

	private ClientTooltipComponent createClientTooltipComponent(TooltipComponent tooltipComponent) {
		if (tooltipComponent instanceof ClientTooltipComponent clientTooltipComponent) {
			return clientTooltipComponent;
		}
		return ClientTooltipComponent.create(tooltipComponent);
	}
}
