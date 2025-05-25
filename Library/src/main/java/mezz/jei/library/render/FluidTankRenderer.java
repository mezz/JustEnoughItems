package mezz.jei.library.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FluidTankRenderer<T> implements IIngredientRenderer<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
	private static final int TEXTURE_SIZE = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	private final IPlatformFluidHelperInternal<T> fluidHelper;
	private final long capacity;
	private final TooltipMode tooltipMode;
	private final int width;
	private final int height;
	private final TilingDirection tilingDirection;

	enum TooltipMode {
		SHOW_AMOUNT,
		SHOW_AMOUNT_AND_CAPACITY,
		ITEM_LIST
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper) {
		this(fluidHelper, fluidHelper.bucketVolume(), TooltipMode.ITEM_LIST, 16, 16, TilingDirection.UP_RIGHT);
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, boolean showCapacity, int width, int height) {
		this(fluidHelper, capacity, showCapacity, width, height, TilingDirection.UP_RIGHT);
	}

	public FluidTankRenderer(
		IPlatformFluidHelperInternal<T> fluidHelper,
		long capacity,
		boolean showCapacity,
		int width,
		int height,
		TilingDirection tilingDirection
	) {
		this(
			fluidHelper,
			capacity,
			showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT,
			width,
			height,
			tilingDirection
		);
	}

	private FluidTankRenderer(
		IPlatformFluidHelperInternal<T> fluidHelper,
		long capacity,
		TooltipMode tooltipMode,
		int width,
		int height,
		TilingDirection tilingDirection
	) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");
		Preconditions.checkArgument(width > 0, "width must be > 0");
		Preconditions.checkArgument(height > 0, "height must be > 0");
		Preconditions.checkNotNull(tilingDirection, "tilingDirection");
		this.fluidHelper = fluidHelper;
		this.capacity = capacity;
		this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
		this.tilingDirection = tilingDirection;
	}

	@Override
	public void render(PoseStack poseStack, T fluidStack) {
		render(poseStack, fluidStack, 0, 0);
	}

	@Override
	public void render(PoseStack poseStack, T ingredient, int posX, int posY) {
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);

		drawFluid(poseStack, width, height, ingredient, posX, posY);

		RenderSystem.setShaderColor(1, 1, 1, 1);

		RenderSystem.disableBlend();
	}

	private void drawFluid(PoseStack poseStack, final int width, final int height, T fluidStack, int posX, int posY) {
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		Fluid fluid = type.getBase(fluidStack);
		if (fluid.isSame(Fluids.EMPTY)) {
			return;
		}

		fluidHelper.getStillFluidSprite(fluidStack)
			.ifPresent(fluidStillSprite -> {
				int fluidColor = fluidHelper.getColorTint(fluidStack);

				long amount = fluidHelper.getAmount(fluidStack);
				long scaledAmount = (amount * height) / capacity;
				if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
					scaledAmount = MIN_FLUID_HEIGHT;
				}
				if (scaledAmount > height) {
					scaledAmount = height;
				}

				if (scaledAmount > 0) {
					drawTiledSprite(poseStack, width, height, fluidColor, scaledAmount, fluidStillSprite, tilingDirection, posX, posY);
				}
			});
	}

	private static void drawTiledSprite(
		PoseStack poseStack,
		final int tiledWidth,
		final int tiledHeight,
		int color,
		long scaledAmount,
		TextureAtlasSprite sprite,
		TilingDirection tilingDirection,
		int posX,
		int posY
	) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		Matrix4f matrix = poseStack.last().pose();

		int scaledHeight = Math.toIntExact(scaledAmount);
		int fluidY = posY + tiledHeight - scaledHeight;
		int xShift = getXShift(tilingDirection, tiledWidth);
		int yShift = getYShift(tilingDirection, scaledHeight);

		drawTiledSpriteClipped(matrix, posX, fluidY, tiledWidth, scaledHeight, xShift, yShift, sprite, color);
	}

	private static void drawTiledSpriteClipped(Matrix4f matrix, int posX, int posY, int tiledWidth, int tiledHeight, int xShift, int yShift, TextureAtlasSprite sprite, int color) {
		for (int x = 0; x < tiledWidth; ) {
			int uOffset = (x + xShift) % TEXTURE_SIZE;
			int width = Math.min(TEXTURE_SIZE - uOffset, tiledWidth - x);

			for (int y = 0; y < tiledHeight; ) {
				int vOffset = (y + yShift) % TEXTURE_SIZE;
				int height = Math.min(TEXTURE_SIZE - vOffset, tiledHeight - y);
				drawTexture(matrix, posX + x, posY + y, sprite, uOffset, vOffset, width, height, color, 100);
				y += height;
			}

			x += width;
		}
	}

	private static int getXShift(TilingDirection tilingDirection, int desiredWidth) {
		return switch (tilingDirection) {
			case DOWN_RIGHT, UP_RIGHT -> 0;
			case DOWN_LEFT, UP_LEFT -> getShift(desiredWidth);
		};
	}

	private static int getYShift(TilingDirection tilingDirection, int desiredHeight) {
		return switch (tilingDirection) {
			case DOWN_RIGHT, DOWN_LEFT -> 0;
			case UP_RIGHT, UP_LEFT -> getShift(desiredHeight);
		};
	}

	private static int getShift(int desired) {
		int remainder = desired % TEXTURE_SIZE;
		if (remainder == 0) {
			return 0;
		}
		return TEXTURE_SIZE - remainder;
	}

	private static void drawTexture(
		Matrix4f matrix,
		float xCoord,
		float yCoord,
		TextureAtlasSprite textureSprite,
		int uOffset,
		int vOffset,
		int width,
		int height,
		int color,
		float zLevel
	) {
		float uScale = (textureSprite.getU1() - textureSprite.getU0()) / TEXTURE_SIZE;
		float vScale = (textureSprite.getV1() - textureSprite.getV0()) / TEXTURE_SIZE;
		float uMin = textureSprite.getU0() + (uOffset * uScale);
		float uMax = uMin + (width * uScale);
		float vMin = textureSprite.getV0() + (vOffset * vScale);
		float vMax = vMin + (height * vScale);

		float alpha = (color >> 24 & 0xFF) / 255F;
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(matrix, xCoord, yCoord + height, zLevel).uv(uMin, vMax).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix, xCoord + width, yCoord + height, zLevel).uv(uMax, vMax).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix, xCoord + width, yCoord, zLevel).uv(uMax, vMin).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix, xCoord, yCoord, zLevel).uv(uMin, vMin).color(red, green, blue, alpha).endVertex();
		tessellator.end();
	}

	@Override
	public List<Component> getTooltip(T fluidStack, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();

		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
		Fluid fluidType = type.getBase(fluidStack);
		try {
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
		} catch (RuntimeException e) {
			Component displayName = fluidHelper.getDisplayName(fluidStack);
			LOGGER.error("Failed to get tooltip for fluid: {}", displayName, e);
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
