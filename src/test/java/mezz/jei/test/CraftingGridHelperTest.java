package mezz.jei.test;

import java.util.Arrays;
import java.util.Collection;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.gui.CraftingGridHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CraftingGridHelperTest {
	@Parameterized.Parameters(name = "{0}x{1}")
	public static Collection<Object[]> recipes() {
		return Arrays.asList(new Object[][]{
			{1, 1, new int[]{4}},
			{1, 2, new int[]{1, 4}},
			{1, 3, new int[]{1, 4, 7}},
			{2, 1, new int[]{3, 4}},
			{2, 2, new int[]{0, 1, 3, 4}},
			{2, 3, new int[]{0, 1, 3, 4, 6, 7}},
			{3, 1, new int[]{3, 4, 5}},
			{3, 2, new int[]{3, 4, 5, 6, 7, 8}},
			{3, 3, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}}
		});
	}

	private final int width;
	private final int height;
	private final int[] expectedIndexes;

	public CraftingGridHelperTest(int width, int height, int[] expectedIndexes) {
		this.width = width;
		this.height = height;
		this.expectedIndexes = expectedIndexes;
	}

	@Test
	public void exposesCenteredGridIndexesWithoutContainerSlotOffsets() {
		ICraftingGridHelper helper = new CraftingGridHelper(10, 20);

		int[] indexes = new int[width * height];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = helper.getCraftingIndex(i, width, height);
		}

		Assert.assertArrayEquals(expectedIndexes, indexes);
	}
}
