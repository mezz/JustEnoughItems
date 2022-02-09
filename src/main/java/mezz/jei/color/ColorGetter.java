package mezz.jei.color;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ColorGetter implements IColorHelper {

	public static final ColorGetter INSTANCE = new ColorGetter();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String[] defaultColors = new String[]{
		"White:EEEEEE",
		"LightBlue:7492cc",
		"Cyan:00EEEE",
		"Blue:2222dd",
		"LapisBlue:25418b",
		"Teal:008080",
		"Yellow:cacb58",
		"GoldenYellow:EED700",
		"Orange:d97634",
		"Pink:D1899D",
		"HotPink:FC0FC0",
		"Magenta:b24bbb",
		"Purple:813eb9",
		"JadedPurple:43324f",
		"EvilPurple:2e1649",
		"Lavender:B57EDC",
		"Indigo:480082",
		"Sand:dbd3a0",
		"Tan:bb9b63",
		"LightBrown:A0522D",
		"Brown:634b33",
		"DarkBrown:3a2d13",
		"LimeGreen:43b239",
		"SlimeGreen:83cb73",
		"Green:008000",
		"DarkGreen:224d22",
		"GrassGreen:548049",
		"Red:963430",
		"BrickRed:b0604b",
		"NetherBrick:2a1516",
		"Redstone:ce3e36",
		"Black:181515",
		"CharcoalGray:464646",
		"IronGray:646464",
		"Gray:808080",
		"Silver:C0C0C0"
	};

	private ColorGetter() {

	}

	public static String[] getColorDefaults() {
		return defaultColors;
	}

	public static List<Integer> getColors(ItemStack itemStack, int colorCount) {
		try {
			return unsafeGetColors(itemStack, colorCount);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.debug("Failed to get color name for {}", itemStackInfo, e);
			return Collections.emptyList();
		}
	}

	private static List<Integer> unsafeGetColors(ItemStack itemStack, int colorCount) {
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

	private static List<Integer> getItemColors(ItemStack itemStack, int colorCount) {
		final ItemColors itemColors = Minecraft.getInstance().getItemColors();
		final int renderColor = itemColors.getColor(itemStack, 0);
		final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(itemStack);
		if (textureAtlasSprite == null) {
			return Collections.emptyList();
		}
		return INSTANCE.getColors(textureAtlasSprite, renderColor, colorCount);
	}

	private static List<Integer> getBlockColors(Block block, int colorCount) {
		BlockState blockState = block.defaultBlockState();
		final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		final int renderColor = blockColors.getColor(blockState, null, null, 0);
		final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(blockState);
		if (textureAtlasSprite == null) {
			return Collections.emptyList();
		}
		return INSTANCE.getColors(textureAtlasSprite, renderColor, colorCount);
	}

	@Override
	public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
		if (colorCount <= 0) {
			return Collections.emptyList();
		}
		final NativeImage bufferedImage = getNativeImage(textureAtlasSprite);
		if (bufferedImage == null) {
			return Collections.emptyList();
		}
		final List<Integer> colors = new ArrayList<>(colorCount);
		final int[][] palette = ColorThief.getPalette(bufferedImage, colorCount, 2, false);
		if (palette != null) {
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
		}
		return colors;
	}

	@Nullable
	private static NativeImage getNativeImage(TextureAtlasSprite textureAtlasSprite) {
		final int iconWidth = textureAtlasSprite.getWidth();
		final int iconHeight = textureAtlasSprite.getHeight();
		final int frameCount = textureAtlasSprite.getFrameCount();
		if (iconWidth <= 0 || iconHeight <= 0 || frameCount <= 0) {
			return null;
		}
		NativeImage[] frames = textureAtlasSprite.mainImage;
		return frames[0];
	}

	@Nullable
	private static TextureAtlasSprite getTextureAtlasSprite(BlockState blockState) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockRenderDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();
		BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
		BakedModel blockModel = blockModelShapes.getBlockModel(blockState);
		TextureAtlasSprite textureAtlasSprite = blockModel.getParticleIcon(EmptyModelData.INSTANCE);
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
		TextureAtlasSprite particleTexture = itemModel.getParticleIcon(EmptyModelData.INSTANCE);
		if (particleTexture instanceof MissingTextureAtlasSprite) {
			return null;
		}
		return particleTexture;
	}
}
