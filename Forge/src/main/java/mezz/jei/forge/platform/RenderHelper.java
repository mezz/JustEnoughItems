package mezz.jei.forge.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
		return bakedModel.getParticleIcon(ModelData.EMPTY);
	}

	@Override
	public ItemColors getItemColors() {
		return Minecraft.getInstance().getItemColors();
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
		guiGraphics.renderComponentTooltipFromElements(font, elements, x, y, stack);
	}

	@Override
	public Component getName(TagKey<?> tagKey) {
		String tagTranslationKey = getTagTranslationKey(tagKey);
		return Component.translatableWithFallback(tagTranslationKey, "#" + tagKey.location());
	}

	@Override
	public BakedModel createLimitedQuadItemModel(BakedModel bakedModel) {
		return ForgeLimitedQuadItemModel.wrap(bakedModel);
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

	private static String getTagTranslationKey(TagKey<?> tagKey) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("tag.");
		ResourceLocation registryIdentifier = tagKey.registry().location();
		ResourceLocation tagIdentifier = tagKey.location();
		if (!registryIdentifier.getNamespace().equals(ModIds.MINECRAFT_ID)) {
			stringBuilder.append(registryIdentifier.getNamespace()).append(".");
		}

		String registryId = ResourceLocationUtil.sanitizePath(registryIdentifier.getPath());
		String tagId = ResourceLocationUtil.sanitizePath(tagIdentifier.getPath());

		stringBuilder.append(registryId)
			.append(".")
			.append(tagIdentifier.getNamespace())
			.append(".")
			.append(tagId);
		return stringBuilder.toString();
	}
}
