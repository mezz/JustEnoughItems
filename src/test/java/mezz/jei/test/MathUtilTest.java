package mezz.jei.test;

import java.awt.Rectangle;
import java.util.Collections;

import mezz.jei.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilTest {
	@Test
	public void negativeExclusionOutsideComparisonAreaDoesNotMoveRectangle() {
		Rectangle comparisonArea = new Rectangle(10, 10, 20, 20);
		Rectangle offscreenExclusion = new Rectangle(-20, -20, 10, 10);

		Rectangle result = MathUtil.moveDownToAvoidIntersection(Collections.singleton(offscreenExclusion), comparisonArea);

		Assert.assertEquals(comparisonArea, result);
	}

	@Test
	public void negativeExclusionOnlyMovesOverlappingRectangle() {
		Rectangle comparisonArea = new Rectangle(10, 10, 20, 20);
		Rectangle overlappingExclusion = new Rectangle(-10, 10, 25, 5);

		Rectangle result = MathUtil.moveDownToAvoidIntersection(Collections.singleton(overlappingExclusion), comparisonArea);

		Assert.assertEquals(new Rectangle(10, 15, 20, 20), result);
	}

	@Test
	public void intersectsOnlyReportsActualIntersectionForNegativeArea() {
		Rectangle comparisonArea = new Rectangle(10, 10, 20, 20);
		Rectangle offscreenExclusion = new Rectangle(-20, -20, 10, 10);
		Rectangle overlappingExclusion = new Rectangle(-10, 10, 25, 5);

		Assert.assertFalse(MathUtil.intersects(Collections.singleton(offscreenExclusion), comparisonArea));
		Assert.assertTrue(MathUtil.intersects(Collections.singleton(overlappingExclusion), comparisonArea));
	}
}
