package mezz.jei.common.util;

public class ImmutablePoint2i {
	public static final ImmutablePoint2i EMPTY = new ImmutablePoint2i(0, 0);

	private final int x;
	private final int y;

	public ImmutablePoint2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ImmutablePoint2i point) {
			return x == point.x && y == point.y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + x;
		hash = hash * 31 + y;
		return hash;
	}
}
