package mezz.jei.common.color;

public final class ColorUtil {
	private ColorUtil() {

	}

	/**
	 * http://www.compuphase.com/cmetric.htm
	 * http://stackoverflow.com/a/6334454
	 * Returns 0 for equal colors, nonzero for colors that look different.
	 * The return value is farther from 0 the more different the colors look.
	 */
	public static double fastPerceptualColorDistanceSquared(int[] color1, int[] color2) {
		final int red1 = color1[0];
		final int red2 = color2[0];
		final int redMean = (red1 + red2) >> 1;
		final int r = red1 - red2;
		final int g = color1[1] - color2[1];
		final int b = color1[2] - color2[2];
		return (((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8);
	}

	/**
	 * http://www.compuphase.com/cmetric.htm
	 * http://stackoverflow.com/a/6334454
	 * Returns 0 for equal colors, nonzero for colors that look different.
	 * The return value is farther from 0 the more different the colors look.
	 * <p>
	 * Weighs the distance from grey more heavily, to avoid matching grey and colorful colors together.
	 */
	public static double slowPerceptualColorDistanceSquared(int color1, int color2) {
		final int red1 = color1 >> 16 & 255;
		final int green1 = color1 >> 8 & 255;
		final int blue1 = color1 & 255;
		final int red2 = color2 >> 16 & 255;
		final int green2 = color2 >> 8 & 255;
		final int blue2 = color2 & 255;
		final int redMean = (red1 + red2) >> 1;
		final int r = red1 - red2;
		final int g = green1 - green2;
		final int b = blue1 - blue2;
		final double colorDistanceSquared = (((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8);

		final double grey1 = (red1 + green1 + blue1) / 3.0;
		final double grey2 = (red2 + green2 + blue2) / 3.0;
		final double greyDistance1 = Math.abs(grey1 - red1) + Math.abs(grey1 - green1) + Math.abs(grey1 - blue1);
		final double greyDistance2 = Math.abs(grey2 - red2) + Math.abs(grey2 - green2) + Math.abs(grey2 - blue2);
		final double greyDistance = greyDistance1 - greyDistance2;

		return colorDistanceSquared + (greyDistance * greyDistance / 10.0);
	}

}
