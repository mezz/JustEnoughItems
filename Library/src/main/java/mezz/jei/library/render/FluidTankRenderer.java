package mezz.jei.library.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix4f;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FluidTankRenderer<T> implements IIngredientRenderer<T> {
	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
	private static final int TEXTURE_SIZE = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	private final IPlatformFluidHelperInternal<T> fluidHelper;
	private final long capacity;
	private final TooltipMode tooltipMode;
	private final int width;
	private final int height;

	enum TooltipMode {
		SHOW_AMOUNT,
		SHOW_AMOUNT_AND_CAPACITY,
		ITEM_LIST
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper) {
		this(fluidHelper, fluidHelper.bucketVolume(), TooltipMode.ITEM_LIST, 16, 16);
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, boolean showCapacity, int width, int height) {
		this(fluidHelper, capacity, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height);
	}

	private FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, TooltipMode tooltipMode, int width, int height) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");
		Preconditions.checkArgument(width > 0, "width must be > 0");
		Preconditions.checkArgument(height > 0, "height must be > 0");
		this.fluidHelper = fluidHelper;
		this.capacity = capacity;
		this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(GuiGraphics guiGraphics, T fluidStack) {
		render(guiGraphics, fluidStack, 0, 0);
	}

	@Override
	public void render(GuiGraphics guiGraphics, T ingredient, int posX, int posY) {
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		Fluid fluid = type.getBase(ingredient);
		if (fluid.isSame(Fluids.EMPTY)) {
			return;
		}

		fluidHelper.getStillFluidSprite(ingredient)
			.ifPresent(fluidStillSprite -> {
				int fluidColor = fluidHelper.getColorTint(ingredient);

				long amount = fluidHelper.getAmount(ingredient);
				long scaledAmount = (amount * height) / capacity;
				if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
					scaledAmount = MIN_FLUID_HEIGHT;
				}
				if (scaledAmount > height) {
					scaledAmount = height;
				}

				drawTiledSprite(guiGraphics, width, height, fluidColor, scaledAmount, fluidStillSprite, posX, posY);
			});
	}

	private static void drawTiledSprite(GuiGraphics guiGraphics, final int tiledWidth, final int tiledHeight, int color, long scaledAmount, TextureAtlasSprite sprite, int posX, int posY) {
		@SuppressWarnings("deprecation")
		RenderType rendertype = RenderType.guiTextured(TextureAtlas.LOCATION_BLOCKS);
		VertexConsumer bufferBuilder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(rendertype);

		Matrix4f matrix = guiGraphics.pose().last().pose();

		final int xTileCount = tiledWidth / TEXTURE_SIZE;
		final int xRemainder = tiledWidth - (xTileCount * TEXTURE_SIZE);
		final long yTileCount = scaledAmount / TEXTURE_SIZE;
		final long yRemainder = scaledAmount - (yTileCount * TEXTURE_SIZE);

		final int yStart = tiledHeight + posY;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : TEXTURE_SIZE;
				long height = (yTile == yTileCount) ? yRemainder : TEXTURE_SIZE;
				int x = posX + (xTile * TEXTURE_SIZE);
				int y = yStart - ((yTile + 1) * TEXTURE_SIZE);
				if (width > 0 && height > 0) {
					long maskTop = TEXTURE_SIZE - height;
					int maskRight = TEXTURE_SIZE - width;

					drawTextureWithMasking(bufferBuilder, matrix, x, y, sprite, color, maskTop, maskRight, 100);
				}
			}
		}
	}

	private static void drawTextureWithMasking(VertexConsumer bufferBuilder, Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int color, long maskTop, long maskRight, float zLevel) {
		float uMin = textureSprite.getU0();
		float uMax = textureSprite.getU1();
		float vMin = textureSprite.getV0();
		float vMax = textureSprite.getV1();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));

		bufferBuilder.addVertex(matrix, xCoord, yCoord + 16, zLevel)
			.setColor(color)
			.setUv(uMin, vMax);
		bufferBuilder.addVertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel)
			.setColor(color)
			.setUv(uMax, vMax);
		bufferBuilder.addVertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel)
			.setColor(color)
			.setUv(uMax, vMin);
		bufferBuilder.addVertex(matrix, xCoord, yCoord + maskTop, zLevel)
			.setColor(color)
			.setUv(uMin, vMin);
	}

	@Override
	public List<Component> getTooltip(T fluidStack, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();

		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		Fluid fluidType = type.getBase(fluidStack);
		if (fluidType.isSame(Fluids.EMPTY)) {
			return tooltip;
		}

		fluidHelper.getTooltip(tooltip, fluidStack, tooltipFlag);

		long amount = fluidHelper.getAmount(fluidStack);
		long milliBuckets = (amount * 1000) / fluidHelper.bucketVolume();

		if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
			MutableComponent amountString = Component.translatable("jei.tooltip.liquid.amount.with.capacity", nf.format(milliBuckets), nf.format(capacity));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		} else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
			MutableComponent amountString = Component.translatable("jei.tooltip.liquid.amount", nf.format(milliBuckets));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		}

		return tooltip;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
