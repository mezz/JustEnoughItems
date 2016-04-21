package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	private final int capacityMb;
	private final boolean showCapacity;
	private final int width;
	private final int height;
	@Nullable
	private final IDrawable overlay;

	public FluidStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
		this.capacityMb = capacityMb;
		this.showCapacity = showCapacity;
		this.width = width;
		this.height = height;
		this.overlay = overlay;
	}

	@Override
	public void setIngredients(@Nonnull Collection<FluidStack> ingredients) {

	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		GlStateManager.disableBlend();
		drawFluid(minecraft, xPosition, yPosition, fluidStack);

		GlStateManager.color(1, 1, 1, 1);

		if (overlay != null) {
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 200);
			overlay.draw(minecraft, xPosition, yPosition);
			GlStateManager.popMatrix();

			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}
	}

	private void drawFluid(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		if (fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
		ResourceLocation fluidStill = fluid.getStill();
		TextureAtlasSprite fluidStillSprite = null;
		if (fluidStill != null) {
			fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
		}
		if (fluidStillSprite == null) {
			fluidStillSprite = textureMapBlocks.getMissingSprite();
		}

		int fluidColor = fluid.getColor(fluidStack);

		int scaledAmount = (fluidStack.amount * height) / capacityMb;
		if (fluidStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > height) {
			scaledAmount = height;
		}

		minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		setGLColorFromInt(fluidColor);

		final int xTileCount = width / TEX_WIDTH;
		final int xRemainder = width - (xTileCount * TEX_WIDTH);
		final int yTileCount = scaledAmount / TEX_HEIGHT;
		final int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

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
		VertexBuffer vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexBuffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
		vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
		vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
		vertexBuffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
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

		String amount;
		if (showCapacity) {
			amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount.with.capacity", fluidStack.amount, capacityMb);
		} else {
			amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount", fluidStack.amount);
		}
		tooltip.add(TextFormatting.GRAY + amount);

		return tooltip;
	}

	@Override
	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull FluidStack fluidStack) {
		return minecraft.fontRendererObj;
	}
}
