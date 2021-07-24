package mezz.jei.util;

import net.minecraft.client.renderer.Rect2i;

public class Rectangle2dBuilder {
	private int x;
	private int y;
	private int width;
	private int height;

	public Rectangle2dBuilder(Rect2i rectangle2d) {
		this.x = rectangle2d.getX();
		this.y = rectangle2d.getY();
		this.width = rectangle2d.getWidth();
		this.height = rectangle2d.getHeight();
	}

	public Rectangle2dBuilder(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rectangle2dBuilder setX(int x) {
		this.x = x;
		return this;
	}

	public Rectangle2dBuilder setX(Rect2i rect) {
		this.x = rect.getX();
		return this;
	}

	public Rectangle2dBuilder addX(int x) {
		this.x += x;
		return this;
	}

	public Rectangle2dBuilder subX(int x) {
		this.x -= x;
		return this;
	}

	public Rectangle2dBuilder setY(int y) {
		this.y = y;
		return this;
	}

	public Rectangle2dBuilder setY(Rect2i rect) {
		this.y = rect.getY();
		return this;
	}

	public Rectangle2dBuilder subY(int y) {
		this.y -= y;
		return this;
	}

	public Rectangle2dBuilder setWidth(int width) {
		this.width = width;
		return this;
	}

	public Rectangle2dBuilder setWidth(Rect2i rect) {
		this.width = rect.getWidth();
		return this;
	}

	public Rectangle2dBuilder addWidth(int width) {
		this.width += width;
		return this;
	}

	public Rectangle2dBuilder subtractWidth(int width) {
		this.width -= width;
		return this;
	}

	public Rectangle2dBuilder setHeight(int height) {
		this.height = height;
		return this;
	}

	public Rectangle2dBuilder setHeight(Rect2i rect) {
		this.height = rect.getHeight();
		return this;
	}

	public Rectangle2dBuilder addHeight(int height) {
		this.height += height;
		return this;
	}

	public Rectangle2dBuilder subtractHeight(int height) {
		this.height -= height;
		return this;
	}

	public Rectangle2dBuilder insetByPadding(int padding) {
		this.x += padding;
		this.y += padding;
		this.width -= padding * 2;
		this.height -= padding * 2;
		return this;
	}

	public Rect2i build() {
		return new Rect2i(x, y, width, height);
	}
}
