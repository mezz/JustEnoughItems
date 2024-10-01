package mezz.jei.common.util;

public record ImmutablePoint2i(int x, int y) {
	public static final ImmutablePoint2i ORIGIN = new ImmutablePoint2i(0, 0);
}
