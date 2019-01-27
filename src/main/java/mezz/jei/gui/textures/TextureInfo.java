package mezz.jei.gui.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class TextureInfo {
	private final JeiTextureMap textureMap;
	private final ResourceLocation spriteLocation;
	private final int width;
	private final int height;
	private int sliceLeft;
	private int sliceRight;
	private int sliceTop;
	private int sliceBottom;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public TextureInfo(JeiTextureMap textureMap, ResourceLocation spriteLocation, int width, int height) {
		this.textureMap = textureMap;
		this.spriteLocation = spriteLocation;

		this.width = width;
		this.height = height;
	}

	public TextureInfo slice(int left, int right, int top, int bottom) {
		this.sliceLeft = left;
		this.sliceRight = right;
		this.sliceTop = top;
		this.sliceBottom = bottom;
		return this;
	}

	public TextureInfo trim(int left, int right, int top, int bottom) {
		this.trimLeft = left;
		this.trimRight = right;
		this.trimTop = top;
		this.trimBottom = bottom;
		return this;
	}

	public ResourceLocation getLocation() {
		return textureMap.getLocation();
	}

	public TextureAtlasSprite getSprite() {
		return this.textureMap.getSprite(spriteLocation);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getSliceLeft() {
		return sliceLeft;
	}

	public int getSliceRight() {
		return sliceRight;
	}

	public int getSliceTop() {
		return sliceTop;
	}

	public int getSliceBottom() {
		return sliceBottom;
	}

	public int getTrimLeft() {
		return trimLeft;
	}

	public int getTrimRight() {
		return trimRight;
	}

	public int getTrimTop() {
		return trimTop;
	}

	public int getTrimBottom() {
		return trimBottom;
	}
}
