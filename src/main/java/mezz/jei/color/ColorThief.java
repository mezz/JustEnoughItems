package mezz.jei.color;

/*
 * Java Color Thief
 * by Sven Woltmann, Fonpit AG
 *
 * http://www.androidpit.com
 * http://www.androidpit.de
 *
 * License
 * -------
 * Creative Commons Attribution 2.5 License:
 * http://creativecommons.org/licenses/by/2.5/
 *
 * Thanks
 * ------
 * Lokesh Dhakar - for the original Color Thief JavaScript version
 * available at http://lokeshdhakar.com/projects/color-thief/
 */

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;

import com.mojang.blaze3d.platform.NativeImage;

public class ColorThief {
	/**
	 * Use the median cut algorithm to cluster similar colors.
	 *
	 * @param sourceImage the source image
	 * @param colorCount  the size of the palette; the number of colors returned
	 * @param quality     0 is the highest quality settings. 10 is the default. There is
	 *                    a trade-off between quality and speed. The bigger the number,
	 *                    the faster the palette generation but the greater the
	 *                    likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 * @return the palette as array of RGB arrays
	 */
	public static int[][] getPalette(NativeImage sourceImage, int colorCount, int quality, boolean ignoreWhite) {
		MMCQ.CMap cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
		if (cmap == null) {
			return new int[0][0];
		}
		return cmap.palette();
	}

	/**
	 * Use the median cut algorithm to cluster similar colors.
	 *
	 * @param sourceImage the source image
	 * @param colorCount  the size of the palette; the number of colors returned
	 * @param quality     0 is the highest quality settings. 10 is the default. There is
	 *                    a trade-off between quality and speed. The bigger the number,
	 *                    the faster the palette generation but the greater the
	 *                    likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 * @return the color map
	 */
	@Nullable
	public static MMCQ.CMap getColorMap(NativeImage sourceImage, int colorCount, int quality, boolean ignoreWhite) {
		if (sourceImage.format() == NativeImage.Format.RGBA) {
			int[][] pixelArray = getPixels(sourceImage, quality, ignoreWhite);
			// Send array to quantize function which clusters values using median
			// cut algorithm
			return MMCQ.quantize(pixelArray, colorCount);
		}
		return null;
	}

	/**
	 * Gets the image's pixels via BufferedImage.getRaster().getDataBuffer().
	 * Fast, but doesn't work for all color models.
	 *
	 * @param sourceImage the source image
	 * @param quality     1 is the highest quality settings. 10 is the default. There is
	 *                    a trade-off between quality and speed. The bigger the number,
	 *                    the faster the palette generation but the greater the
	 *                    likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 * @return an array of pixels (each an RGB int array)
	 */
	private static int[][] getPixels(NativeImage sourceImage, int quality, boolean ignoreWhite) {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		int pixelCount = width * height;

		// Store the RGB values in an array format suitable for quantize function

		// numRegardedPixels must be rounded up to avoid an
		// ArrayIndexOutOfBoundsException if all pixels are good.
		int numRegardedPixels = (pixelCount + quality - 1) / quality;

		int numUsedPixels = 0;
		int[][] pixelArray = new int[numRegardedPixels][];

		int i = 0;
		while (i < pixelCount) {
			int x = i % width;
			int y = i / width;
			int rgba = sourceImage.getPixelRGBA(x, y);
			int a = rgba >> 24 & 255;
			int b = rgba >> 16 & 255;
			int g = rgba >> 8 & 255;
			int r = rgba & 255;
			// If pixel is mostly opaque and not white
			if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
				pixelArray[numUsedPixels] = new int[]{r, g, b};
				numUsedPixels++;
				i += quality;
			} else {
				i++;
			}
		}
		// trim the array
		return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
	}
}
