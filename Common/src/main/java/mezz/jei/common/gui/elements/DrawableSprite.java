package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.VertexConsumer;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class DrawableSprite implements IDrawableStatic {
	private final JeiSpriteUploader spriteUploader;
	private final ResourceLocation location;
	private final int width;
	private final int height;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location, int width, int height) {
		this.spriteUploader = spriteUploader;
		this.location = location;
		this.width = width;
		this.height = height;
	}

	public DrawableSprite trim(int left, int right, int top, int bottom) {
		this.trimLeft = left;
		this.trimRight = right;
		this.trimTop = top;
		this.trimBottom = bottom;
		return this;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteUploader.getSprite(location);
		int textureWidth = this.width;
		int textureHeight = this.height;

		RenderType rendertype = RenderType.guiTextured(Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);
		VertexConsumer bufferBuilder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(rendertype);

		maskTop += trimTop;
		maskBottom += trimBottom;
		maskLeft += trimLeft;
		maskRight += trimRight;

		int x = xOffset + maskLeft;
		int y = yOffset + maskTop;
		int width = textureWidth - maskRight - maskLeft;
		int height = textureHeight - maskBottom - maskTop;
		float uSize = sprite.getU1() - sprite.getU0();
		float vSize = sprite.getV1() - sprite.getV0();

		float minU = sprite.getU0() + uSize * (maskLeft / (float) textureWidth);
		float minV = sprite.getV0() + vSize * (maskTop / (float) textureHeight);
		float maxU = sprite.getU1() - uSize * (maskRight / (float) textureWidth);
		float maxV = sprite.getV1() - vSize * (maskBottom / (float) textureHeight);

		Matrix4f matrix = guiGraphics.pose().last().pose();
		bufferBuilder.addVertex(matrix, x, y + height, 0)
			.setColor(255, 255, 255, 255)
			.setUv(minU, maxV);
		bufferBuilder.addVertex(matrix, x + width, y + height, 0)
			.setColor(255, 255, 255, 255)
			.setUv(maxU, maxV);
		bufferBuilder.addVertex(matrix, x + width, y, 0)
			.setColor(255, 255, 255, 255)
			.setUv(maxU, minV);
		bufferBuilder.addVertex(matrix, x, y, 0)
			.setColor(255, 255, 255, 255)
			.setUv(minU, minV);
	}
}
