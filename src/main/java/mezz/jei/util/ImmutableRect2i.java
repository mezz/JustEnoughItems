package mezz.jei.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nonnegative;

public class ImmutableRect2i {
	public static final ImmutableRect2i EMPTY = new ImmutableRect2i(0, 0, 0, 0);

	@Nonnegative
	private final int x;
	@Nonnegative
	private final int y;
	@Nonnegative
	private final int width;
	@Nonnegative
	private final int height;

	public ImmutableRect2i(Rect2i rect) {
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	@SuppressWarnings("ConstantConditions")
	public ImmutableRect2i(@Nonnegative int x, @Nonnegative int y, @Nonnegative int width, @Nonnegative int height) {
		Preconditions.checkArgument(x >= 0, "x must be greater or equal 0");
		Preconditions.checkArgument(y >= 0, "y must be greater or equal 0");
		Preconditions.checkArgument(width >= 0, "width must be greater or equal 0");
		Preconditions.checkArgument(height >= 0, "height must be greater or equal 0");
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Nonnegative
	public int getX() {
		return x;
	}

	@Nonnegative
	public int getY() {
		return y;
	}

	@Nonnegative
	public int getWidth() {
		return width;
	}

	@Nonnegative
	public int getHeight() {
		return height;
	}

	public boolean isEmpty() {
		return width == 0 || height == 0;
	}

	public ImmutableRect2i moveRight(@Nonnegative int x) {
		return new ImmutableRect2i(this.x + x, this.y, this.width, this.height);
	}

	public ImmutableRect2i moveLeft(@Nonnegative int x) {
		return new ImmutableRect2i(this.x - x, this.y, this.width, this.height);
	}

	public ImmutableRect2i moveDown(@Nonnegative int y) {
		return new ImmutableRect2i(this.x, this.y + y, this.width, this.height);
	}

	public ImmutableRect2i moveUp(@Nonnegative int y) {
		return new ImmutableRect2i(this.x, this.y - y, this.width, this.height);
	}

	public ImmutableRect2i insetByPadding(@Nonnegative int padding) {
		return new ImmutableRect2i(this.x + padding, this.y + padding, this.width - (padding * 2), this.height - (padding * 2));
	}

	public ImmutableRect2i expandByPadding(@Nonnegative int padding) {
		return new ImmutableRect2i(this.x - padding, this.y - padding, this.width + (padding * 2), this.height + (padding * 2));
	}

	public ImmutableRect2i cropRight(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x, this.y, this.width - amount, this.height);
	}

	public ImmutableRect2i cropLeft(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x + amount, this.y, this.width - amount, this.height);
	}

	public ImmutableRect2i cropBottom(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x, this.y, this.width, this.height - amount);
	}

	public ImmutableRect2i cropTop(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x, this.y + amount, this.width, this.height - amount);
	}

	public ImmutableRect2i keepTop(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x, this.y, this.width, amount);
	}

	public ImmutableRect2i keepBottom(@Nonnegative int amount) {
		int cropAmount = this.height - amount;
		return cropTop(cropAmount);
	}

	public ImmutableRect2i keepRight(@Nonnegative int amount) {
		int cropAmount = this.width - amount;
		return cropLeft(cropAmount);
	}

	public ImmutableRect2i keepLeft(@Nonnegative int amount) {
		return new ImmutableRect2i(this.x, this.y, amount, this.height);
	}

	public ImmutableRect2i addOffset(@Nonnegative int x, @Nonnegative int y) {
		return new ImmutableRect2i(this.x + x, this.y + y, this.width, this.height);
	}

	public ImmutableRect2i matchWidthAndX(ImmutableRect2i rect) {
		return new ImmutableRect2i(rect.getX(), this.y, rect.getWidth(), this.height);
	}

	public MutableRect2i toMutable() {
		return new MutableRect2i(this);
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
}
