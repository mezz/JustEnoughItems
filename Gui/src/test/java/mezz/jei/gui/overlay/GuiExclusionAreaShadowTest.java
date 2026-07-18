package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuiExclusionAreaShadowTest {
	@Test
	public void shadowOnlyDrawsForExclusionsIntersectingBackground() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(10, 10, 40, 40);
		ImmutableRect2i farExclusion = new ImmutableRect2i(100, 10, 20, 20);
		ImmutableRect2i touchingExclusion = new ImmutableRect2i(50, 10, 20, 20);

		List<ImmutableRect2i> shadowAreas = GuiExclusionAreaShadow.calculateShadowAreas(
			backgroundArea,
			Set.of(farExclusion, touchingExclusion)
		);

		assertTrue(shadowAreas.isEmpty());
	}

	@Test
	public void shadowAreaContainsExclusionArea() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(0, 0, 100, 100);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(30, 30, 20, 15);

		List<ImmutableRect2i> shadowAreas = GuiExclusionAreaShadow.calculateShadowAreas(
			backgroundArea,
			Set.of(exclusionArea)
		);

		assertEquals(List.of(
			new ImmutableRect2i(
				exclusionArea.x() - GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.y() - GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.width() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.height() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE
			)
		), shadowAreas);
		assertContainedBy(exclusionArea, shadowAreas.getFirst());
	}

	@Test
	public void shadowAreaIsNotMovedWhenExclusionTouchesBackgroundEdge() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(10, 10, 30, 30);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(8, 8, 10, 10);

		List<ImmutableRect2i> shadowAreas = GuiExclusionAreaShadow.calculateShadowAreas(
			backgroundArea,
			Set.of(exclusionArea)
		);

		assertEquals(List.of(
			new ImmutableRect2i(
				exclusionArea.x() - GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.y() - GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.width() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE,
				exclusionArea.height() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE
			)
		), shadowAreas);
		assertFalse(backgroundArea.contains(shadowAreas.getFirst().x(), shadowAreas.getFirst().y()));
	}

	@Test
	public void shadowTextureCanBeNineSliced() throws IOException {
		URL resource = GuiExclusionAreaShadowTest.class.getClassLoader()
			.getResource("assets/jei/textures/jei/atlas/gui/exclusion_area_shadow.png");
		assertNotNull(resource);

		BufferedImage image = ImageIO.read(resource);

		assertEquals(16, image.getWidth());
		assertEquals(16, image.getHeight());
		assertEquals(0, alpha(image.getRGB(0, 0)));
		assertSolidCenter(image);
		assertTileableEdges(image);
	}

	private static int alpha(int argb) {
		return (argb >>> 24) & 0xFF;
	}

	private static void assertSolidCenter(BufferedImage image) {
		int centerAlpha = alpha(image.getRGB(GuiExclusionAreaShadow.SHADOW_SIZE, GuiExclusionAreaShadow.SHADOW_SIZE));
		assertTrue(centerAlpha > 0);

		for (int y = GuiExclusionAreaShadow.SHADOW_SIZE; y < image.getHeight() - GuiExclusionAreaShadow.SHADOW_SIZE; y++) {
			for (int x = GuiExclusionAreaShadow.SHADOW_SIZE; x < image.getWidth() - GuiExclusionAreaShadow.SHADOW_SIZE; x++) {
				assertEquals(centerAlpha, alpha(image.getRGB(x, y)));
			}
		}
	}

	private static void assertTileableEdges(BufferedImage image) {
		int right = image.getWidth() - GuiExclusionAreaShadow.SHADOW_SIZE;
		int bottom = image.getHeight() - GuiExclusionAreaShadow.SHADOW_SIZE;

		for (int y = 0; y < GuiExclusionAreaShadow.SHADOW_SIZE; y++) {
			assertConstantHorizontalAlpha(image, y, GuiExclusionAreaShadow.SHADOW_SIZE, right);
			assertConstantHorizontalAlpha(image, image.getHeight() - y - 1, GuiExclusionAreaShadow.SHADOW_SIZE, right);
		}

		for (int x = 0; x < GuiExclusionAreaShadow.SHADOW_SIZE; x++) {
			assertConstantVerticalAlpha(image, x, GuiExclusionAreaShadow.SHADOW_SIZE, bottom);
			assertConstantVerticalAlpha(image, image.getWidth() - x - 1, GuiExclusionAreaShadow.SHADOW_SIZE, bottom);
		}
	}

	private static void assertConstantHorizontalAlpha(BufferedImage image, int y, int startX, int endX) {
		int expectedAlpha = alpha(image.getRGB(startX, y));
		for (int x = startX + 1; x < endX; x++) {
			assertEquals(expectedAlpha, alpha(image.getRGB(x, y)));
		}
	}

	private static void assertConstantVerticalAlpha(BufferedImage image, int x, int startY, int endY) {
		int expectedAlpha = alpha(image.getRGB(x, startY));
		for (int y = startY + 1; y < endY; y++) {
			assertEquals(expectedAlpha, alpha(image.getRGB(x, y)));
		}
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.x() >= outer.x(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.y() >= outer.y(), () -> inner + " should not start above " + outer);
		assertTrue(inner.x() + inner.width() <= outer.x() + outer.width(), () -> inner + " should not extend right of " + outer);
		assertTrue(inner.y() + inner.height() <= outer.y() + outer.height(), () -> inner + " should not extend below " + outer);
	}
}
