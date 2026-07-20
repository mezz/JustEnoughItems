package mezz.jei.test;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.library.color.ColorThief;
import net.minecraft.util.FastColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorThiefTest {
	private static final int WIDTH = 4;
	private static final int HEIGHT = 4;
	private static final int CHANNEL_TOLERANCE = 4;

	@Test
	public void extractsUniformNonBlackColors() {
		assertUniformColorPalette(0xFFFF0000);
		assertUniformColorPalette(0xFF00FF00);
		assertUniformColorPalette(0xFF0000FF);
		assertUniformColorPalette(0xFF444444);
	}

	@Test
	public void extractsDistinctColorsFromSimpleImage() {
		try (NativeImage image = createImage(4, 4)) {
			fillRect(image, 0, 0, 2, 4, 0xFFFF0000);
			fillRect(image, 2, 0, 2, 4, 0xFF00FF00);

			int[][] palette = ColorThief.getPalette(image, 2, 1, false);

			Assertions.assertEquals(2, palette.length);
			assertPaletteContains(palette, 0xFFFF0000);
			assertPaletteContains(palette, 0xFF00FF00);
		}
	}

	@Test
	public void extractsDistinctColorsFromFourQuadrants() {
		try (NativeImage image = createImage(4, 4)) {
			fillRect(image, 0, 0, 2, 2, 0xFFFF0000);
			fillRect(image, 2, 0, 2, 2, 0xFF00FF00);
			fillRect(image, 0, 2, 2, 2, 0xFF0000FF);
			fillRect(image, 2, 2, 2, 2, 0xFFFFFFFF);

			int[][] palette = ColorThief.getPalette(image, 4, 1, false);

			Assertions.assertEquals(4, palette.length);
			assertPaletteContains(palette, 0xFFFF0000);
			assertPaletteContains(palette, 0xFF00FF00);
			assertPaletteContains(palette, 0xFF0000FF);
			assertPaletteContains(palette, 0xFFFFFFFF);
		}
	}

	@Test
	public void ignoresTransparentPixels() {
		try (NativeImage image = createImage(0x00000000)) {
			int[][] palette = ColorThief.getPalette(image, 4, 1, false);

			Assertions.assertEquals(0, palette.length);
		}
	}

	@Test
	public void ignoresPixelsBelowAlphaThreshold() {
		try (NativeImage image = createImage(2, 1)) {
			setPixel(image, 0, 0, 0x7CFF0000);
			setPixel(image, 1, 0, 0xFF0000FF);

			int[][] palette = ColorThief.getPalette(image, 2, 1, false);

			Assertions.assertEquals(1, palette.length);
			assertColorCloseTo(0xFF0000FF, palette[0]);
		}
	}

	@Test
	public void keepsPixelsAtAlphaThreshold() {
		try (NativeImage image = createImage(0x7DFF0000)) {
			int[][] palette = ColorThief.getPalette(image, 4, 1, false);

			Assertions.assertEquals(1, palette.length);
			assertColorCloseTo(0xFFFF0000, palette[0]);
		}
	}

	@Test
	public void ignoresWhitePixelsWhenRequested() {
		try (NativeImage image = createImage(2, 1)) {
			setPixel(image, 0, 0, 0xFFFFFFFF);
			setPixel(image, 1, 0, 0xFFFF0000);

			int[][] palette = ColorThief.getPalette(image, 2, 1, true);

			Assertions.assertEquals(1, palette.length);
			assertColorCloseTo(0xFFFF0000, palette[0]);
		}
	}

	@Test
	public void keepsWhitePixelsByDefault() {
		try (NativeImage image = createImage(2, 1)) {
			setPixel(image, 0, 0, 0xFFFFFFFF);
			setPixel(image, 1, 0, 0xFFFF0000);

			int[][] palette = ColorThief.getPalette(image, 2, 1, false);

			Assertions.assertEquals(2, palette.length);
			assertPaletteContains(palette, 0xFFFFFFFF);
			assertPaletteContains(palette, 0xFFFF0000);
		}
	}

	@Test
	public void usesQualityAsSamplingStride() {
		try (NativeImage image = createImage(4, 1)) {
			setPixel(image, 0, 0, 0xFFFF0000);
			setPixel(image, 1, 0, 0xFF00FF00);
			setPixel(image, 2, 0, 0xFFFF0000);
			setPixel(image, 3, 0, 0xFF00FF00);

			int[][] palette = ColorThief.getPalette(image, 2, 2, false);

			Assertions.assertEquals(1, palette.length);
			assertColorCloseTo(0xFFFF0000, palette[0]);
		}
	}

	@Test
	public void throwsForInvalidQuality() {
		try (NativeImage image = createImage(2, 1)) {
			Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> ColorThief.getPalette(image, 2, 0, false)
			);
			Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> ColorThief.getPalette(image, 2, -1, false)
			);
			Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> ColorThief.getColorMap(image, 2, 0, false)
			);
		}
	}

	@Test
	public void returnsEmptyPaletteForInvalidColorCounts() {
		try (NativeImage image = createImage(0xFFFF0000)) {
			Assertions.assertEquals(0, ColorThief.getPalette(image, 0, 1, false).length);
			Assertions.assertEquals(0, ColorThief.getPalette(image, -1, 1, false).length);
			Assertions.assertEquals(0, ColorThief.getPalette(image, 257, 1, false).length);
		}
	}

	@Test
	public void returnsEmptyPaletteForUnsupportedImageFormat() {
		try (NativeImage image = new NativeImage(NativeImage.Format.LUMINANCE, WIDTH, HEIGHT, true)) {
			int[][] palette = ColorThief.getPalette(image, 4, 1, false);

			Assertions.assertEquals(0, palette.length);
		}
	}

	private static void assertUniformColorPalette(int argbColor) {
		try (NativeImage image = createImage(argbColor)) {
			int[][] palette = ColorThief.getPalette(image, 4, 1, false);

			Assertions.assertEquals(1, palette.length);
			assertColorCloseTo(argbColor, palette[0]);
		}
	}

	private static NativeImage createImage(int argbColor) {
		NativeImage image = createImage(WIDTH, HEIGHT);
		fillRect(image, 0, 0, WIDTH, HEIGHT, argbColor);
		return image;
	}

	private static NativeImage createImage(int width, int height) {
		NativeImage image = new NativeImage(width, height, false);
		fillRect(image, 0, 0, width, height, 0x00000000);
		return image;
	}

	private static void fillRect(NativeImage image, int x, int y, int width, int height, int argbColor) {
		for (int dx = 0; dx < width; dx++) {
			for (int dy = 0; dy < height; dy++) {
				setPixel(image, x + dx, y + dy, argbColor);
			}
		}
	}

	private static void setPixel(NativeImage image, int x, int y, int argbColor) {
		image.setPixelRGBA(x, y, FastColor.ABGR32.fromArgb32(argbColor));
	}

	private static void assertPaletteContains(int[][] palette, int expectedArgbColor) {
		for (int[] color : palette) {
			if (isColorCloseTo(expectedArgbColor, color)) {
				return;
			}
		}
		Assertions.fail("Expected palette to contain color close to %08X".formatted(expectedArgbColor));
	}

	private static void assertColorCloseTo(int expectedArgbColor, int[] actualColor) {
		Assertions.assertTrue(
			isColorCloseTo(expectedArgbColor, actualColor),
			() -> "Expected color close to %08X, got [%s, %s, %s]".formatted(
				expectedArgbColor,
				actualColor[0],
				actualColor[1],
				actualColor[2]
			)
		);
	}

	private static boolean isColorCloseTo(int expectedArgbColor, int[] actualColor) {
		Assertions.assertEquals(3, actualColor.length);
		return isChannelClose(FastColor.ARGB32.red(expectedArgbColor), actualColor[0]) &&
			isChannelClose(FastColor.ARGB32.green(expectedArgbColor), actualColor[1]) &&
			isChannelClose(FastColor.ARGB32.blue(expectedArgbColor), actualColor[2]);
	}

	private static boolean isChannelClose(int expected, int actual) {
		return Math.abs(expected - actual) <= CHANNEL_TOLERANCE;
	}
}
