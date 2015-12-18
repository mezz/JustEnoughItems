package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IDrawable;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private final int capacityMb;
	private final int width;
	private final int height;
	@Nullable
	private final IDrawable overlay;

	public FluidStackRenderer(int capacityMb, int width, int height, @Nullable IDrawable overlay) {
		this.capacityMb = capacityMb;
		this.width = width;
		this.height = height;
		this.overlay = overlay;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nonnull FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		GlStateManager.pushAttrib();
		{
			GlStateManager.disableBlend();
			TextureAtlasSprite fluidStillSprite = minecraft.getTextureMapBlocks().getTextureExtry(fluid.getStill().toString());

			int fluidColor = fluid.getColor(fluidStack);

			int scaledLiquid = (fluidStack.amount * height) / capacityMb;
			if (scaledLiquid > height) {
				scaledLiquid = height;
			}

			minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			setGLColorFromInt(fluidColor);

			final int xTileCount = width / TEX_WIDTH;
			final int xRemainder = width - (xTileCount * TEX_WIDTH);
			final int yTileCount = scaledLiquid / TEX_HEIGHT;
			final int yRemainder = scaledLiquid - (yTileCount * TEX_HEIGHT);

			final int yStart = yPosition + height;

			for (int xTile = 0; xTile <= xTileCount; xTile++) {
				for (int yTile = 0; yTile <= yTileCount; yTile++) {
					int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
					int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
					int x = xPosition + (xTile * TEX_WIDTH);
					int y = yStart - ((yTile + 1) * TEX_HEIGHT);
					if (width > 0 && height > 0) {
						int maskTop = TEX_HEIGHT - height;
						int maskRight = TEX_WIDTH - width;

						drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 100);
					}
				}
			}

			GlStateManager.resetColor();

			if (overlay != null) {
				GlStateManager.pushAttrib();
				GlStateManager.pushMatrix();
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.translate(0, 0, 200);
				overlay.draw(minecraft, xPosition, yPosition);
				GlStateManager.enableDepth();
				GlStateManager.popMatrix();
				GlStateManager.popAttrib();
			}
			GlStateManager.enableBlend();
		}
		GlStateManager.popAttrib();
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		GlStateManager.color(red, green, blue, 1.0F);
	}

	private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
		double uMin = (double) textureSprite.getMinU();
		double uMax = (double) textureSprite.getMaxU();
		double vMin = (double) textureSprite.getMinV();
		double vMax = (double) textureSprite.getMaxV();
		uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
		vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.startDrawingQuads();
		worldrenderer.addVertexWithUV(xCoord, yCoord + 16, zLevel, uMin,  vMax);
		worldrenderer.addVertexWithUV(xCoord + 16 - maskRight, yCoord + 16, zLevel, uMax, vMax);
		worldrenderer.addVertexWithUV(xCoord + 16 - maskRight, yCoord + maskTop, zLevel, uMax, vMin);
		worldrenderer.addVertexWithUV(xCoord, yCoord + maskTop,  zLevel, uMin, vMin);
		tessellator.draw();
	}

	@Nonnull
	@Override
	public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull FluidStack fluidStack) {
		List<String> tooltip = new ArrayList<>();
		Fluid fluidType = fluidStack.getFluid();
		if (fluidType == null) {
			return tooltip;
		}

		String fluidName = fluidType.getLocalizedName(fluidStack);
		tooltip.add(fluidName);

		String amount = String.format(Locale.ENGLISH, EnumChatFormatting.GRAY + "%,d / %,d", fluidStack.amount, capacityMb);
		tooltip.add(amount);

		return tooltip;
	}

	@Override
	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull FluidStack fluidStack) {
		return minecraft.fontRendererObj;
	}
}
