package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LookupHistoryOverlayTest {
	@Test
	public void lineSegmentsSkipIntersectingExclusionArea() {
		ImmutableRect2i lineArea = new ImmutableRect2i(10, 20, 100, 1);
		Set<ImmutableRect2i> exclusions = Set.of(new ImmutableRect2i(40, 15, 20, 10));

		List<LookupHistoryOverlay.LineSegment> lineSegments = LookupHistoryOverlay.calculateLineSegments(
			lineArea,
			exclusions
		);

		assertEquals(
			List.of(
				new LookupHistoryOverlay.LineSegment(10, 40),
				new LookupHistoryOverlay.LineSegment(60, 110)
			),
			lineSegments
		);
	}

	@Test
	public void lineSegmentsIgnoreExclusionsThatDoNotIntersectLine() {
		ImmutableRect2i lineArea = new ImmutableRect2i(10, 20, 100, 1);
		Set<ImmutableRect2i> exclusions = Set.of(
			new ImmutableRect2i(40, 19, 20, 1),
			new ImmutableRect2i(40, 21, 20, 1),
			new ImmutableRect2i(110, 20, 20, 1)
		);

		List<LookupHistoryOverlay.LineSegment> lineSegments = LookupHistoryOverlay.calculateLineSegments(
			lineArea,
			exclusions
		);

		assertEquals(List.of(new LookupHistoryOverlay.LineSegment(10, 110)), lineSegments);
	}

	@Test
	public void lineSegmentsMergeOverlappingClippedExclusionAreas() {
		ImmutableRect2i lineArea = new ImmutableRect2i(10, 20, 100, 1);
		Set<ImmutableRect2i> exclusions = Set.of(
			new ImmutableRect2i(0, 20, 20, 1),
			new ImmutableRect2i(40, 20, 20, 1),
			new ImmutableRect2i(50, 20, 30, 1),
			new ImmutableRect2i(100, 20, 40, 1)
		);

		List<LookupHistoryOverlay.LineSegment> lineSegments = LookupHistoryOverlay.calculateLineSegments(
			lineArea,
			exclusions
		);

		assertEquals(
			List.of(
				new LookupHistoryOverlay.LineSegment(20, 40),
				new LookupHistoryOverlay.LineSegment(80, 100)
			),
			lineSegments
		);
	}
}
