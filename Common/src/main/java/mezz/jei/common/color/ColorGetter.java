package mezz.jei.common.color;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.api.helpers.IColorHelper;
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

public final class ColorGetter implements IColorHelper {

	public static final ColorGetter INSTANCE = new ColorGetter();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final List<ColorName> defaultColors = List.of(
		new ColorName("White", 0xEEEEEE),
		new ColorName("LightBlue", 0x7492cc),
		new ColorName("Cyan", 0x00EEEE),
		new ColorName("Blue", 0x2222dd),
		new ColorName("LapisBlue", 0x25418b),
		new ColorName("Teal", 0x008080),
		new ColorName("Yellow", 0xcacb58),
		new ColorName("GoldenYellow", 0xEED700),
		new ColorName("Orange", 0xd97634),
		new ColorName("Pink", 0xD1899D),
		new ColorName("HotPink", 0xFC0FC0),
		new ColorName("Magenta", 0xb24bbb),
		new ColorName("Purple", 0x813eb9),
		new ColorName("EvilPurple", 0x2e1649),
		new ColorName("Lavender", 0xB57EDC),
		new ColorName("Indigo", 0x480082),
		new ColorName("Sand", 0xdbd3a0),
		new ColorName("Tan", 0xbb9b63),
		new ColorName("LightBrown", 0xA0522D),
		new ColorName("Brown", 0x634b33),
		new ColorName("DarkBrown", 0x3a2d13),
		new ColorName("LimeGreen", 0x43b239),
		new ColorName("SlimeGreen", 0x83cb73),
		new ColorName("Green", 0x008000),
		new ColorName("DarkGreen", 0x224d22),
		new ColorName("GrassGreen", 0x548049),
		new ColorName("Red", 0x963430),
		new ColorName("BrickRed", 0xb0604b),
		new ColorName("NetherBrick", 0x2a1516),
		new ColorName("Redstone", 0xce3e36),
		new ColorName("Black", 0x181515),
		new ColorName("CharcoalGray", 0x464646),
		new ColorName("IronGray", 0x646464),
		new ColorName("Gray", 0x808080),
		new ColorName("Silver", 0xC0C0C0)
	);

	private ColorGetter() {

	}

	public static List<ColorName> getColorDefaults() {
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
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		final ItemColors itemColors = renderHelper.getItemColors();
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
	}

	@Nullable
	private static NativeImage getNativeImage(TextureAtlasSprite textureAtlasSprite) {
		final int iconWidth = textureAtlasSprite.getWidth();
		final int iconHeight = textureAtlasSprite.getHeight();
		if (iconWidth <= 0 || iconHeight <= 0) {
			return null;
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
