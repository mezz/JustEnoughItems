package mezz.jei.library.color;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ColorGetter {
	private static final Logger LOGGER = LogManager.getLogger();

	public ColorGetter() {

	}

	public List<Integer> getColors(ItemStack itemStack, int colorCount) {
		try {
			return unsafeGetColors(itemStack, colorCount);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.debug("Failed to get color name for {}", itemStackInfo, e);
			return Collections.emptyList();
		}
	}

	private List<Integer> unsafeGetColors(ItemStack itemStack, int colorCount) {
		final Item item = itemStack.getItem();
		if (itemStack.isEmpty()) {
			return Collections.emptyList();
		} else if (item instanceof final BlockItem itemBlock) {
			final Block block = itemBlock.getBlock();
			//noinspection ConstantConditions
			if (block == null) {
				return Collections.emptyList();
			}
			return getBlockColors(block, colorCount);
		} else {
			return getItemColors(itemStack, colorCount);
		}
	}

	private List<Integer> getItemColors(ItemStack itemStack, int colorCount) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		final ItemColors itemColors = renderHelper.getItemColors();
		final int renderColor = itemColors.getColor(itemStack, 0);
		final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(itemStack);
		if (textureAtlasSprite == null) {
			return Collections.emptyList();
		}
		return getColors(textureAtlasSprite, renderColor, colorCount);
	}

	private List<Integer> getBlockColors(Block block, int colorCount) {
		BlockState blockState = block.defaultBlockState();
		final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		final int renderColor = blockColors.getColor(blockState, null, null, 0);
		final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(blockState);
		if (textureAtlasSprite == null) {
			return Collections.emptyList();
		}
		return getColors(textureAtlasSprite, renderColor, colorCount);
	}

	public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
		if (colorCount <= 0) {
			return Collections.emptyList();
		}
		return getNativeImage(textureAtlasSprite)
			.map(bufferedImage -> {
				final List<Integer> colors = new ArrayList<>(colorCount);
				final int[][] palette = ColorThief.getPalette(bufferedImage, colorCount, 2, false);
				for (int[] colorInt : palette) {
					int red = (int) ((colorInt[0] - 1) * (float) (renderColor >> 16 & 255) / 255.0F);
					int green = (int) ((colorInt[1] - 1) * (float) (renderColor >> 8 & 255) / 255.0F);
					int blue = (int) ((colorInt[2] - 1) * (float) (renderColor & 255) / 255.0F);
					red = Mth.clamp(red, 0, 255);
					green = Mth.clamp(green, 0, 255);
					blue = Mth.clamp(blue, 0, 255);
					int color = ((0xFF) << 24) |
						((red & 0xFF) << 16) |
						((green & 0xFF) << 8) |
						(blue & 0xFF);
					colors.add(color);
				}
				return colors;
			})
			.orElseGet(Collections::emptyList);
	}

	private static Optional<NativeImage> getNativeImage(TextureAtlasSprite textureAtlasSprite) {
		final int iconWidth = textureAtlasSprite.getWidth();
		final int iconHeight = textureAtlasSprite.getHeight();
		if (iconWidth <= 0 || iconHeight <= 0) {
			return Optional.empty();
		}

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getMainImage(textureAtlasSprite);
	}

	@Nullable
	private static TextureAtlasSprite getTextureAtlasSprite(BlockState blockState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockRenderDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();
		BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
		BakedModel blockModel = blockModelShapes.getBlockModel(blockState);
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		TextureAtlasSprite textureAtlasSprite = renderHelper.getParticleIcon(blockModel);
		if (textureAtlasSprite instanceof MissingTextureAtlasSprite) {
			return null;
		}
		return textureAtlasSprite;
	}

	@Nullable
	private static TextureAtlasSprite getTextureAtlasSprite(ItemStack itemStack) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		ItemModelShaper itemModelMesher = itemRenderer.getItemModelShaper();
		BakedModel itemModel = itemModelMesher.getItemModel(itemStack);
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		TextureAtlasSprite particleTexture = renderHelper.getParticleIcon(itemModel);
		if (particleTexture instanceof MissingTextureAtlasSprite) {
			return null;
		}
		return particleTexture;
	}
}
