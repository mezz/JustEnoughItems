package mezz.jei.common.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nonnegative;

public class ImmutableRect2i {
	public static final ImmutableRect2i EMPTY = new ImmutableRect2i(0, 0, 0, 0);

	private final int x;
	private final int y;
	@Nonnegative
	private final int width;
	@Nonnegative
	private final int height;

	public ImmutableRect2i(Rect2i rect) {
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public ImmutableRect2i(int x, int y, int width, int height) {
		Preconditions.checkArgument(width >= 0, "width must be >= 0");
		Preconditions.checkArgument(height >= 0, "height must be >= 0");
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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
		return new ImmutableRect2i(this.x + x, this.y, this.width, this.height);
	}

	public ImmutableRect2i moveLeft(int x) {
		if (x == 0) {
			return this;
		}
		return new ImmutableRect2i(this.x - x, this.y, this.width, this.height);
	}

	public ImmutableRect2i moveDown(int y) {
		if (y == 0) {
			return this;
		}
		return new ImmutableRect2i(this.x, this.y + y, this.width, this.height);
	}

	public ImmutableRect2i moveUp(int y) {
		if (y == 0) {
			return this;
		}
		return new ImmutableRect2i(this.x, this.y - y, this.width, this.height);
	}

	public ImmutableRect2i insetBy(int amount) {
		if (amount == 0) {
			return this;
		}
		amount = Math.min(amount, this.width / 2);
		amount = Math.min(amount, this.height / 2);
		return new ImmutableRect2i(
			this.x + amount,
			this.y + amount,
			this.width - amount * 2,
			this.height - amount * 2
		);
	}

	public ImmutableRect2i expandBy(int amount) {
		if (amount == 0) {
			return this;
		}
		return new ImmutableRect2i(this.x - amount, this.y - amount, this.width + (amount * 2), this.height + (amount * 2));
	}

	public ImmutableRect2i cropRight(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.width) {
			amount = this.width;
		}
		return new ImmutableRect2i(this.x, this.y, this.width - amount, this.height);
	}

	public ImmutableRect2i cropLeft(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.width) {
			amount = this.width;
		}
		return new ImmutableRect2i(this.x + amount, this.y, this.width - amount, this.height);
	}

	public ImmutableRect2i cropBottom(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.height) {
			amount = this.height;
		}
		return new ImmutableRect2i(this.x, this.y, this.width, this.height - amount);
	}

	public ImmutableRect2i cropTop(int amount) {
		if (amount == 0) {
			return this;
		}
		if (amount > this.height) {
			amount = this.height;
		}
		return new ImmutableRect2i(this.x, this.y + amount, this.width, this.height - amount);
	}

	public ImmutableRect2i keepTop(int amount) {
		if (amount == this.height) {
			return this;
		}
		if (amount > this.height) {
			return this;
		}
		return new ImmutableRect2i(this.x, this.y, this.width, amount);
	}

	public ImmutableRect2i keepBottom(int amount) {
		if (amount == this.height) {
			return this;
		}
		if (amount > this.height) {
			return this;
		}
		int cropAmount = this.height - amount;
		return new ImmutableRect2i(this.x, this.y + cropAmount, this.width, amount);
	}

	public ImmutableRect2i keepRight(int amount) {
		if (amount == this.width) {
			return this;
		}
		if (amount > this.width) {
			return this;
		}
		int cropAmount = this.width - amount;
		return new ImmutableRect2i(this.x + cropAmount, this.y, amount, this.height);
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
		return new ImmutableRect2i(this.x + x, this.y + y, this.width, this.height);
	}

	public ImmutableRect2i matchWidthAndX(ImmutableRect2i rect) {
		return new ImmutableRect2i(rect.getX(), this.y, rect.getWidth(), this.height);
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof ImmutableRect2i other) {
			return
				x == other.x &&
				y == other.y &&
				width == other.width &&
				height == other.height;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + x;
		hash = hash * 31 + y;
		hash = hash * 31 + width;
		hash = hash * 31 + height;
		return hash;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("x", x)
			.add("y", y)
			.add("width", width)
			.add("height", height)
			.toString();
	}

	public Rect2i toMutable() {
		return new Rect2i(x, y, width, height);
	}
}
