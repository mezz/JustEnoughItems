package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import org.joml.Matrix4f;

public class DrawableResource implements IDrawableStatic {

	private final ResourceLocation resourceLocation;
	private final int textureWidth;
	private final int textureHeight;

	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int paddingTop;
	private final int paddingBottom;
	private final int paddingLeft;
	private final int paddingRight;

	public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight, int textureWidth, int textureHeight) {
		this.resourceLocation = resourceLocation;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;

		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
	}

	@Override
	public int getWidth() {
		return width + paddingLeft + paddingRight;
	}

	@Override
	public int getHeight() {
		return height + paddingTop + paddingBottom;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.resourceLocation);

		int x = xOffset + this.paddingLeft + maskLeft;
		int y = yOffset + this.paddingTop + maskTop;
		int u = this.u + maskLeft;
		int v = this.v + maskTop;
		int width = this.width - maskRight - maskLeft;
		int height = this.height - maskBottom - maskTop;
		float f = 1.0F / this.textureWidth;
		float f1 = 1.0F / this.textureHeight;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix = guiGraphics.pose().last().pose();
		bufferbuilder.addVertex(matrix, x, y + height, 0).setUv(u * f, (v + (float) height) * f1);
		bufferbuilder.addVertex(matrix, x + width, y + height, 0).setUv((u + (float) width) * f, (v + (float) height) * f1);
		bufferbuilder.addVertex(matrix, x + width, y, 0).setUv((u + (float) width) * f, v * f1);
		bufferbuilder.addVertex(matrix, x, y, 0).setUv(u * f, v * f1);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
	}
}
