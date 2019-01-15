package mezz.jei.gui.textures;

import mezz.jei.util.Log;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class TextureInfo {
	private final JeiTextureMap textureMap;
	private final TextureAtlasSprite sprite;
	private final int width;
	private final int height;

	public TextureInfo(JeiTextureMap textureMap, TextureAtlasSprite sprite, int width, int height) {
		this.textureMap = textureMap;
		this.sprite = sprite;

		this.width = width;
		this.height = height;
	}

	public JeiTextureMap getTextureMap() {
		return textureMap;
	}

	public TextureAtlasSprite getSprite() {
		return sprite;
	}

	public int getScale() {
		int xScale = sprite.getIconWidth() / width;
		int yScale = sprite.getIconHeight() / height;
		if (xScale != yScale || xScale * width != sprite.getIconWidth() || xScale == 0) {
			Log.get().error("Texture has the wrong dimensions. Expected a multiple of: ({}x{}) {}", width, height, sprite);
			return 1;
		}
		return xScale;
	}
}
