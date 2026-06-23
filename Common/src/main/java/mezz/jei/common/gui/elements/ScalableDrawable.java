package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class ScalableDrawable implements IScalableDrawable {
	private final Supplier<TextureAtlasSprite> spriteSupplier;

	public ScalableDrawable(JeiSpriteUploader spriteUploader, ResourceLocation spriteId) {
		this(() -> spriteUploader.getSprite(spriteId));
	}

	public ScalableDrawable(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId));
	}

	ScalableDrawable(Supplier<TextureAtlasSprite> spriteSupplier) {
		this.spriteSupplier = spriteSupplier;
	}

	public void draw(GuiGraphics guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteSupplier.get();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, sprite.atlasLocation());

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix = guiGraphics.pose().last().pose();
		bufferBuilder.vertex(matrix, xOffset, yOffset + height, 0)
			.uv(sprite.getU0(), sprite.getV1())
			.endVertex();
		bufferBuilder.vertex(matrix, xOffset + width, yOffset + height, 0)
			.uv(sprite.getU1(), sprite.getV1())
			.endVertex();
		bufferBuilder.vertex(matrix, xOffset + width, yOffset, 0)
			.uv(sprite.getU1(), sprite.getV0())
			.endVertex();
		bufferBuilder.vertex(matrix, xOffset, yOffset, 0)
			.uv(sprite.getU0(), sprite.getV0())
			.endVertex();
		tessellator.end();
	}
}
