package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ScalableDrawable implements IScalableDrawable {
	private final Supplier<TextureAtlasSprite> spriteSupplier;
	private final ResourceLocation atlasLocation;

	public ScalableDrawable(JeiSpriteUploader spriteUploader, ResourceLocation spriteId) {
		this(() -> spriteUploader.getSprite(spriteId), Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);
	}

	public ScalableDrawable(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId), textureAtlas.location());
	}

	ScalableDrawable(Supplier<TextureAtlasSprite> spriteSupplier, ResourceLocation atlasLocation) {
		this.spriteSupplier = spriteSupplier;
		this.atlasLocation = atlasLocation;
	}

	public void draw(PoseStack poseStack, ImmutableRect2i area) {
		draw(poseStack, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteSupplier.get();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, atlasLocation);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix = poseStack.last().pose();
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
