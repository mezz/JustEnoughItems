package mezz.jei.util;

import net.minecraft.client.renderer.Rectangle2d;

public class Rectangle2dBuilder {
	private int x;
	private int y;
	private int width;
	private int height;

	public Rectangle2dBuilder(Rectangle2d rectangle2d) {
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

	public Rectangle2dBuilder setX(Rectangle2d rect) {
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

	public Rectangle2dBuilder setY(Rectangle2d rect) {
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

	public Rectangle2dBuilder setWidth(Rectangle2d rect) {
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

	public Rectangle2dBuilder setHeight(Rectangle2d rect) {
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

	public Rectangle2d build() {
		return new Rectangle2d(x, y, width, height);
	}
}
