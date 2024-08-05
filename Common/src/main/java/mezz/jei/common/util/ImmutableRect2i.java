package mezz.jei.common.util;

import com.google.common.base.Preconditions;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nonnegative;

@SuppressWarnings("unused")
public record ImmutableRect2i(
	int x,
	int y,
	@Nonnegative int width,
	@Nonnegative int height
) {
	public static final ImmutableRect2i EMPTY = new ImmutableRect2i(0, 0, 0, 0);

	public ImmutableRect2i(Rect2i rect) {
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	@SuppressWarnings("ConstantValue")
	public ImmutableRect2i {
		Preconditions.checkArgument(width >= 0, "width must be >= 0");
		Preconditions.checkArgument(height >= 0, "height must be >= 0");
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isEmpty() {
		return width == 0 || height == 0;
	}

	public boolean contains(ImmutablePoint2i point) {
		return contains(point.x(), point.y());
	}

	public boolean contains(double x, double y) {
		return x >= this.x &&
			y >= this.y &&
			x < this.x + this.width &&
			y < this.y + this.height;
	}

	public boolean intersects(ImmutableRect2i rect) {
		if (this.isEmpty() || rect.isEmpty()) {
			return false;
		}
		return rect.getX() + rect.getWidth() > x &&
			rect.getY() + rect.getHeight() > y &&
			rect.getX() < x + width &&
			rect.getY() < y + height;
	}

	public ImmutableRect2i moveRight(int x) {
		if (x == 0) {
			return this;
		}
		return new ImmutableRect2i(
			Math.addExact(this.x, x),
			this.y,
			this.width,
			this.height
		);
	}

	public ImmutableRect2i moveLeft(int x) {
		if (x == 0) {
			return this;
		}
		return new ImmutableRect2i(
			Math.subtractExact(this.x, x),
			this.y,
			this.width,
			this.height
		);
	}

	public ImmutableRect2i moveDown(int y) {
		if (y == 0) {
			return this;
		}
		return new ImmutableRect2i(
			this.x,
			Math.addExact(this.y, y),
			this.width,
			this.height
		);
	}

	public ImmutableRect2i moveUp(int y) {
		if (y == 0) {
			return this;
		}
		return new ImmutableRect2i(
			this.x,
			Math.subtractExact(this.y, y),
			this.width,
			this.height
		);
	}

	public ImmutableRect2i insetBy(int amount) {
		if (amount == 0) {
			return this;
		}
		amount = Math.min(amount, this.width / 2);
		amount = Math.min(amount, this.height / 2);
		int doubleAmount = Math.multiplyExact(amount, 2);
		return new ImmutableRect2i(
			Math.addExact(this.x, amount),
			Math.addExact(this.y, amount),
			Math.subtractExact(this.width, doubleAmount),
			Math.subtractExact(this.height, doubleAmount)
		);
	}

	public ImmutableRect2i expandBy(int amount) {
		if (amount == 0) {
			return this;
		}
		int doubleAmount = Math.multiplyExact(amount, 2);
		return new ImmutableRect2i(
			Math.subtractExact(this.x, amount),
			Math.subtractExact(y, amount),
			Math.addExact(this.width, doubleAmount),
			Math.addExact(this.height, doubleAmount)
		);
	}

	public ImmutableRect2i cropRight(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.width) {
			amount = this.width;
		}
		return new ImmutableRect2i(
			this.x,
			this.y,
			Math.subtractExact(this.width, amount),
			this.height
		);
	}

	public ImmutableRect2i cropLeft(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.width) {
			amount = this.width;
		}
		return new ImmutableRect2i(
			Math.addExact(this.x, amount),
			this.y,
			Math.subtractExact(this.width, amount),
			this.height
		);
	}

	public ImmutableRect2i cropBottom(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.height) {
			amount = this.height;
		}
		return new ImmutableRect2i(
			this.x,
			this.y,
			this.width,
			Math.subtractExact(this.height, amount)
		);
	}

	public ImmutableRect2i cropTop(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.height) {
			amount = this.height;
		}
		return new ImmutableRect2i(
			this.x,
			Math.addExact(this.y, amount),
			this.width,
			Math.subtractExact(this.height, amount)
		);
	}

	public ImmutableRect2i keepTop(@Nonnegative int amount) {
		if (amount == this.height) {
			return this;
		}
		if (amount > this.height) {
			return this;
		}
		return new ImmutableRect2i(
			this.x,
			this.y,
			this.width,
			amount
		);
	}

	public ImmutableRect2i keepBottom(int amount) {
		if (amount == this.height) {
			return this;
		}
		if (amount > this.height) {
			return this;
		}
		int cropAmount = Math.subtractExact(this.height, amount);
		return new ImmutableRect2i(
			this.x,
			Math.addExact(this.y, cropAmount),
			this.width,
			amount
		);
	}

	public ImmutableRect2i keepRight(int amount) {
		if (amount == this.width) {
			return this;
		}
		if (amount > this.width) {
			return this;
		}
		int cropAmount = Math.subtractExact(this.width, amount);
		return new ImmutableRect2i(
			Math.addExact(this.x, cropAmount),
			this.y,
			amount,
			this.height
		);
	}

	public ImmutableRect2i keepLeft(int amount) {
		if (amount == this.width) {
			return this;
		}
		if (amount > this.width) {
			return this;
		}
		return new ImmutableRect2i(this.x, this.y, amount, this.height);
	}

	public ImmutableRect2i addOffset(int x, int y) {
		return new ImmutableRect2i(
			Math.addExact(this.x, x),
			Math.addExact(this.y, y),
			this.width,
			this.height
		);
	}

	public ImmutableRect2i setPosition(int x, int y) {
		if (x() == x && y() == y) {
			return this;
		}
		return new ImmutableRect2i(
			x,
			y,
			width(),
			height()
		);
	}

	public ImmutableRect2i matchWidthAndX(ImmutableRect2i rect) {
		return new ImmutableRect2i(rect.getX(), this.y, rect.getWidth(), this.height);
	}

	public Rect2i toMutable() {
		return new Rect2i(x, y, width, height);
	}

	public ImmutablePoint2i getPosition() {
		return new ImmutablePoint2i(x, y);
	}

	public ScreenPosition getScreenPosition() {
		return new ScreenPosition(x, y);
	}
}
