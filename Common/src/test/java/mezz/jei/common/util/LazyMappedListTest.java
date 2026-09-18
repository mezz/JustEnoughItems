package mezz.jei.common.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LazyMappedListTest {
	@Test
	void largeListOnlyPreparesVisibleEntriesAndReusesThemAcrossFrames() {
		List<Integer> source = IntStream.range(0, 10_000).boxed().toList();
		List<Integer> mapped = new ArrayList<>();
		List<String> list = new LazyMappedList<>(source, index -> {
			mapped.add(index);
			return "entry " + index;
		}, 40);

		assertEquals(10_000, list.size());
		assertEquals(List.of(), mapped);
		for (int frame = 0; frame < 3; frame++) {
			for (int index = 0; index < 40; index++) {
				assertEquals("entry " + index, list.get(index));
			}
		}
		assertEquals(source.subList(0, 40), mapped);

		// Scrolling one row reuses the overlapping rows and prepares only ten new entries.
		for (int index = 10; index < 50; index++) {
			assertEquals("entry " + index, list.get(index));
		}
		assertEquals(source.subList(0, 50), mapped);

		// Jumping to the last entry does not prepare the intervening thousands of entries.
		assertEquals("entry 9999", list.get(9999));
		assertEquals(51, mapped.size());
		// The cache is bounded: entries scrolled out of view are prepared again if revisited.
		assertEquals("entry 0", list.get(0));
		assertEquals(52, mapped.size());
	}

	@Test
	void invalidIndexesDoNotInvokeTheMapper() {
		List<String> list = new LazyMappedList<>(List.of(1), value -> {
			throw new AssertionError("invalid indexes must not prepare an entry");
		}, 40);
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
	}
}
