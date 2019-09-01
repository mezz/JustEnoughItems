package mezz.jei.plugins.vanilla.ingredients.fluid;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.Translator;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	private final int capacityMb;
	private final TooltipMode tooltipMode;
	private final int width;
	private final int height;
	@Nullable
	private final IDrawable overlay;

	enum TooltipMode {
		SHOW_AMOUNT,
		SHOW_AMOUNT_AND_CAPACITY,
		ITEM_LIST
	}

	public FluidStackRenderer() {
		this(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
	}

	public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
		this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height, overlay);
	}

	public FluidStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
		this.capacityMb = capacityMb;
		this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
		this.overlay = overlay;
	}

	@Override
	public void render(final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();

		drawFluid(xPosition, yPosition, fluidStack);

		GlStateManager.color4f(1, 1, 1, 1);

		if (overlay != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 200);
			overlay.draw(xPosition, yPosition);
			GlStateManager.popMatrix();
		}

		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();
	}

	private void drawFluid(final int xPosition, final int yPosition, FluidStack fluidStack) {
		if (fluidStack.isEmpty()) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluid);

		int fluidColor = fluid.getAttributes().getColor(fluidStack);

		int scaledAmount = (fluidStack.getAmount() * height) / capacityMb;
		if (fluidStack.getAmount() > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > height) {
			scaledAmount = height;
		}

		drawTiledSprite(xPosition, yPosition, width, height, fluidColor, scaledAmount, fluidStillSprite);
	}

	private void drawTiledSprite(final int xPosition, final int yPosition, final int tiledWidth, final int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		setGLColorFromInt(color);

		final int xTileCount = tiledWidth / TEX_WIDTH;
		final int xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
		final int yTileCount = scaledAmount / TEX_HEIGHT;
		final int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

		final int yStart = yPosition + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
				int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
				int x = xPosition + (xTile * TEX_WIDTH);
				int y = yStart - ((yTile + 1) * TEX_HEIGHT);
				if (width > 0 && height > 0) {
					int maskTop = TEX_HEIGHT - height;
					int maskRight = TEX_WIDTH - width;

					drawTextureWithMasking(x, y, sprite, maskTop, maskRight, 100);
				}
			}
		}
	}

	private static TextureAtlasSprite getStillFluidSprite(Fluid fluid) {
		Minecraft minecraft = Minecraft.getInstance();
		AtlasTexture textureMapBlocks = minecraft.getTextureMap();
		ResourceLocation fluidStill = fluid.getAttributes().getStillTexture();
		return textureMapBlocks.getSprite(fluidStill);
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		GlStateManager.color4f(red, green, blue, 1.0F);
	}

	private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
		double uMin = (double) textureSprite.getMinU();
		double uMax = (double) textureSprite.getMaxU();
		double vMin = (double) textureSprite.getMinV();
		double vMax = (double) textureSprite.getMaxV();
		uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
		vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
		bufferBuilder.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
		bufferBuilder.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
		bufferBuilder.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
		tessellator.draw();
	}

	@Override
	public List<String> getTooltip(FluidStack fluidStack, ITooltipFlag tooltipFlag) {
		List<String> tooltip = new ArrayList<>();
		Fluid fluidType = fluidStack.getFluid();
		if (fluidType == null) {
			return tooltip;
		}

		String fluidName = fluidStack.getDisplayName().getFormattedText();
		tooltip.add(fluidName);

		if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
			String amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount.with.capacity", fluidStack.getAmount(), capacityMb);
			tooltip.add(TextFormatting.GRAY + amount);
		} else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
			String amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount", fluidStack.getAmount());
			tooltip.add(TextFormatting.GRAY + amount);
		}

		return tooltip;
	}
}
