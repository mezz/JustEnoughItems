package mezz.jei.gui.textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class TextureInfo {
	private final ResourceLocation location;
	private final TextureAtlasSprite sprite;
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

	public TextureInfo(ResourceLocation location, TextureAtlasSprite sprite, int width, int height) {
		this.location = location;
		this.sprite = sprite;
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
		this.trimLeft = sliceLeft;
		this.trimRight = sliceRight;
		this.trimTop = sliceTop;
		this.trimBottom = sliceBottom;
		return this;
	}

	public ResourceLocation getLocation() {
		return location;
	}

	public TextureAtlasSprite getSprite() {
		return sprite;
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
