package mezz.jei.plugins.vanilla.ingredients.fluid;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.text.NumberFormat;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.InventoryMenu;
import com.mojang.math.Matrix4f;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {
	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
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
	public void render(PoseStack poseStack, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		RenderSystem.enableBlend();

		drawFluid(poseStack, xPosition, yPosition, fluidStack);

		RenderSystem.setShaderColor(1, 1, 1, 1);

		if (overlay != null) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 200);
			overlay.draw(poseStack, xPosition, yPosition);
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
	}

	private void drawFluid(PoseStack poseStack, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
		if (fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);

		FluidAttributes attributes = fluid.getAttributes();
		int fluidColor = attributes.getColor(fluidStack);

		int amount = fluidStack.getAmount();
		int scaledAmount = (amount * height) / capacityMb;
		if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > height) {
			scaledAmount = height;
		}

		drawTiledSprite(poseStack, xPosition, yPosition, width, height, fluidColor, scaledAmount, fluidStillSprite);
	}

	private static void drawTiledSprite(PoseStack poseStack, final int xPosition, final int yPosition, final int tiledWidth, final int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		Matrix4f matrix = poseStack.last().pose();
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

					drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
				}
			}
		}
	}

	private static TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
		Minecraft minecraft = Minecraft.getInstance();
		Fluid fluid = fluidStack.getFluid();
		FluidAttributes attributes = fluid.getAttributes();
		ResourceLocation fluidStill = attributes.getStillTexture(fluidStack);
		return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.setShaderColor(red, green, blue, alpha);
	}

	private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel) {
		float uMin = textureSprite.getU0();
		float uMax = textureSprite.getU1();
		float vMin = textureSprite.getV0();
		float vMax = textureSprite.getV1();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));

		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
		bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
		tessellator.end();
	}

	@Override
	public List<Component> getTooltip(FluidStack fluidStack, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		Fluid fluidType = fluidStack.getFluid();
		if (fluidType == null) {
			return tooltip;
		}

		Component displayName = fluidStack.getDisplayName();
		tooltip.add(displayName);

		int amount = fluidStack.getAmount();
		if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
			TranslatableComponent amountString = new TranslatableComponent("jei.tooltip.liquid.amount.with.capacity", nf.format(amount), nf.format(capacityMb));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		} else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
			TranslatableComponent amountString = new TranslatableComponent("jei.tooltip.liquid.amount", nf.format(amount));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		}

		return tooltip;
	}
}
