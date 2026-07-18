package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuiExclusionAreaShadowTest {
	@Test
	public void shadowOnlyDrawsForExclusionsOverBackground() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(10, 10, 40, 40);
		ImmutableRect2i farExclusion = new ImmutableRect2i(100, 10, 20, 20);
		ImmutableRect2i touchingExclusion = new ImmutableRect2i(50, 10, 20, 20);

		List<GuiExclusionAreaShadow.ShadowBand> bands = GuiExclusionAreaShadow.calculateShadowBands(
			backgroundArea,
			Set.of(farExclusion, touchingExclusion)
		);

		assertTrue(bands.isEmpty());
	}

	@Test
	public void shadowFillsExclusionArea() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(0, 0, 100, 100);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(30, 30, 20, 15);

		List<GuiExclusionAreaShadow.ShadowBand> bands = GuiExclusionAreaShadow.calculateShadowBands(
			backgroundArea,
			Set.of(exclusionArea)
		);

		assertFalse(bands.isEmpty());
		assertTrue(bands.stream().anyMatch(band -> band.area().equals(exclusionArea)));
		for (GuiExclusionAreaShadow.ShadowBand band : bands) {
			assertContainedBy(band.area(), backgroundArea);
		}
	}

	@Test
	public void shadowBandsAreClippedToBackgroundArea() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(10, 10, 30, 30);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(8, 8, 10, 10);

		List<GuiExclusionAreaShadow.ShadowBand> bands = GuiExclusionAreaShadow.calculateShadowBands(
			backgroundArea,
			Set.of(exclusionArea)
		);

		assertFalse(bands.isEmpty());
		assertTrue(bands.stream().anyMatch(band -> band.area().equals(new ImmutableRect2i(10, 10, 8, 8))));
		assertTrue(bands.stream().anyMatch(band -> band.area().x() == backgroundArea.x()));
		assertTrue(bands.stream().anyMatch(band -> band.area().y() == backgroundArea.y()));
		for (GuiExclusionAreaShadow.ShadowBand band : bands) {
			assertContainedBy(band.area(), backgroundArea);
		}
	}

	@Test
	public void shadowExtendsEvenlyAroundCenteredExclusionArea() {
		ImmutableRect2i backgroundArea = new ImmutableRect2i(0, 0, 100, 100);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(40, 45, 10, 8);

		List<GuiExclusionAreaShadow.ShadowBand> bands = GuiExclusionAreaShadow.calculateShadowBands(
			backgroundArea,
			Set.of(exclusionArea)
		);
		ImmutableRect2i shadowBounds = bands.stream()
			.map(GuiExclusionAreaShadow.ShadowBand::area)
			.reduce(MathUtil::union)
			.orElse(ImmutableRect2i.EMPTY);

		assertEquals(exclusionArea.x() - GuiExclusionAreaShadow.SHADOW_SIZE, shadowBounds.x());
		assertEquals(exclusionArea.y() - GuiExclusionAreaShadow.SHADOW_SIZE, shadowBounds.y());
		assertEquals(
			exclusionArea.width() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE,
			shadowBounds.width()
		);
		assertEquals(
			exclusionArea.height() + 2 * GuiExclusionAreaShadow.SHADOW_SIZE,
			shadowBounds.height()
		);
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.x() >= outer.x(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.y() >= outer.y(), () -> inner + " should not start above " + outer);
		assertTrue(inner.x() + inner.width() <= outer.x() + outer.width(), () -> inner + " should not extend right of " + outer);
		assertTrue(inner.y() + inner.height() <= outer.y() + outer.height(), () -> inner + " should not extend below " + outer);
	}
}
