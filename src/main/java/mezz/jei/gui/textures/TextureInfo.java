package mezz.jei.gui.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class TextureInfo {
	private final JeiTextureMap textureMap;
	private final ResourceLocation spriteLocation;
	private final int width;
	private final int height;

	public TextureInfo(JeiTextureMap textureMap, ResourceLocation spriteLocation, int width, int height) {
		this.textureMap = textureMap;
		this.spriteLocation = spriteLocation;

		this.width = width;
		this.height = height;
	}

	public JeiTextureMap getTextureMap() {
		return textureMap;
	}

	public TextureAtlasSprite getSprite() {
		return this.textureMap.getSprite(spriteLocation);
	}

	public int getScale() {
		TextureAtlasSprite sprite = getSprite();
		int xScale = sprite.getWidth() / width;
		int yScale = sprite.getHeight() / height;
		if (xScale != yScale || xScale * width != sprite.getWidth()) {
			throw new IllegalArgumentException("Texture has the wrong dimensions. Expected a multiple of: (" + width + "x" + height + ") " + sprite);
		}
		return xScale;
	}
}
