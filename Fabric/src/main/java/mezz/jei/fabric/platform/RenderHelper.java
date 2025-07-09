package mezz.jei.fabric.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
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

	@Nullable
	@Override
	public TextureAtlasSprite getTextureAtlasSprite(BlockState blockState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockRenderDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();
		BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
		BakedModel blockModel = blockModelShapes.getBlockModel(blockState);
		TextureAtlasSprite textureAtlasSprite = getParticleIcon(blockModel);
		if (textureAtlasSprite.atlasLocation().equals(MissingTextureAtlasSprite.getLocation())) {
			return null;
		}
		return textureAtlasSprite;
	}

	@Override
	public TextureAtlasSprite getParticleIcon(BakedModel bakedModel) {
		return bakedModel.getParticleIcon();
	}

	@Override
	public ItemColors getItemColors() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.itemColors;
	}

	@Override
	public void blitSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int uWidth, int vHeight) {
		guiGraphics.blitSprite(sprite, textureWidth, textureHeight, uPosition, vPosition, x, y, 0, uWidth, vHeight);
	}

	@Override
	public void blitNineSlicedSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice scaling, int xOffset, int yOffset, int width, int height) {
		guiGraphics.blitNineSlicedSprite(sprite, scaling, xOffset, yOffset, 0, width, height);
	}

	@Override
	public void blitTiledSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, GuiSpriteScaling.Tile scaling, int xOffset, int yOffset, int width, int height, int color) {
		setColor(guiGraphics, color);
		{
			guiGraphics.blitTiledSprite(
				sprite,
				xOffset,
				yOffset,
				0,
				width,
				height,
				0,
				0,
				scaling.width(),
				scaling.height(),
				scaling.width(),
				scaling.height()
			);
		}
		guiGraphics.setColor(1, 1, 1, 1);
	}

	private static void setColor(GuiGraphics guiGraphics, int color) {
		float alpha = (color >> 24 & 0xFF) / 255F;
		float red = (color >> 16 & 0xFF) / 255F;
		float green = (color >> 8 & 0xFF) / 255F;
		float blue = (color & 0xFF) / 255F;
		guiGraphics.setColor(red, green, blue, alpha);
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
	public void renderTooltip(GuiGraphics guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack) {
		List<ClientTooltipComponent> components = elements.stream()
			.flatMap(e -> e.map(
				text -> font.split(text, 400).stream().map(ClientTooltipComponent::create),
				tooltipComponent -> Stream.of(createClientTooltipComponent(tooltipComponent))
			))
			.collect(Collectors.toCollection(ArrayList::new));

		guiGraphics.renderTooltipInternal(font, components, x, y, DefaultTooltipPositioner.INSTANCE);
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

	@Override
	public BakedModel createLimitedQuadItemModel(BakedModel bakedModel) {
		return FabricLimitedQuadItemModel.wrap(bakedModel);
	}
}
