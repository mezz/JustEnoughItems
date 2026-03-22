package mezz.jei.fabric.platform;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

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
		ModelManager modelManager = minecraft.getModelManager();
		BlockStateModelSet blockStateModelSet = modelManager.getBlockStateModelSet();
		Material.Baked material = blockStateModelSet.getParticleMaterial(blockState);
		TextureAtlasSprite textureAtlasSprite = material.sprite();
		if (textureAtlasSprite.atlasLocation().equals(MissingTextureAtlasSprite.getLocation())) {
			return null;
		}
		return textureAtlasSprite;
	}

	@Override
	public void blitSprite(GuiGraphicsExtractor guiGraphics, RenderPipeline renderPipeline, TextureAtlasSprite sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int uWidth, int vHeight) {
		guiGraphics.blitSprite(renderPipeline, sprite, textureWidth, textureHeight, uPosition, vPosition, x, y, uWidth, vHeight, -1);
	}

	@Override
	public void blitNineSlicedSprite(GuiGraphicsExtractor guiGraphics, RenderPipeline renderPipeline, TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice scaling, int xOffset, int yOffset, int width, int height) {
		guiGraphics.blitNineSlicedSprite(renderPipeline, sprite, scaling, xOffset, yOffset, width, height, -1);
	}

	@Override
	public void blitTiledSprite(GuiGraphicsExtractor guiGraphics, RenderPipeline renderPipeline, TextureAtlasSprite sprite, GuiSpriteScaling.Tile scaling, int xOffset, int yOffset, int width, int height, int color) {
		guiGraphics.blitTiledSprite(
			renderPipeline,
			sprite,
			xOffset,
			yOffset,
			width,
			height,
			0,
			0,
			scaling.width(),
			scaling.height(),
			scaling.width(),
			scaling.height(),
			color
		);
	}

	@Override
	public Optional<NativeImage> getMainImage(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		NativeImage[] frames = contents.byMipLevel;
		if (frames.length == 0) {
			return Optional.empty();
		}
		NativeImage frame = frames[0];
		return Optional.ofNullable(frame);
	}

	@Override
	public void renderTooltip(GuiGraphicsExtractor guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack) {
		List<ClientTooltipComponent> components = elements.stream()
			.flatMap(e -> e.map(
				text -> font.split(text, 400).stream().map(ClientTooltipComponent::create),
				tooltipComponent -> Stream.of(createClientTooltipComponent(tooltipComponent))
			))
			.collect(Collectors.toCollection(ArrayList::new));

		guiGraphics.setTooltipForNextFrameInternal(font, components, x, y, DefaultTooltipPositioner.INSTANCE, null, true);
	}

	@Override
	public Component getName(TagKey<?> tagKey) {
		return tagKey.getName();
	}

	private ClientTooltipComponent createClientTooltipComponent(TooltipComponent tooltipComponent) {
		if (tooltipComponent instanceof ClientTooltipComponent clientTooltipComponent) {
			return clientTooltipComponent;
		}
		return ClientTooltipComponent.create(tooltipComponent);
	}
}
