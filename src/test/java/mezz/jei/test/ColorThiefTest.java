package mezz.jei.test;

import mezz.jei.color.ColorThief;
import mezz.jei.color.MMCQ;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ColorThiefTest {
	@Test
	public void rejectsInvalidSamplingQuality() {
		assertThrows(IllegalArgumentException.class, () -> ColorThief.getPalette(null, 2, 0, false));
		assertThrows(IllegalArgumentException.class, () -> ColorThief.getPalette(null, 2, -1, false));
	}

	@Test
	public void extractsPaletteFromDescendingChannelValues() {
		int[][] pixels = {
			{255, 0, 0},
			{128, 0, 0}
		};
		MMCQ.CMap colorMap = MMCQ.quantize(pixels, 2);

		assertEquals(2, colorMap.palette().length);
	}
}
