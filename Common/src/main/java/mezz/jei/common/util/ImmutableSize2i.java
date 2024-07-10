package mezz.jei.common.util;

import javax.annotation.Nonnegative;

public class ImmutableSize2i {
	public static final ImmutableSize2i EMPTY = new ImmutableSize2i(0, 0);

	@Nonnegative
	private final int width;
	@Nonnegative
	private final int height;

	public ImmutableSize2i(@Nonnegative int width, @Nonnegative int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getArea() {
		return width * height;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ImmutableSize2i size) {
			return width == size.width && height == size.height;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + width;
		hash = hash * 31 + hash;
		return hash;
	}
}
