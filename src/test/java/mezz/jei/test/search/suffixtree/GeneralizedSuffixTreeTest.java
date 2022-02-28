package mezz.jei.test.search.suffixtree;

import com.google.common.collect.ImmutableSet;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class GeneralizedSuffixTreeTest {

	private static <T> Set<T> search(GeneralizedSuffixTree<T> tree, String word) {
		Set<T> results = new HashSet<>();
		tree.getSearchResults(word, results);
		return results;
	}

	@Test
	public void testSearch() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("a", 0);
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "a"));

		tree.put("ab", 1);
		Assertions.assertEquals(ImmutableSet.of(1), search(tree, "ab"));
		Assertions.assertEquals(ImmutableSet.of(1), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0, 1), search(tree, "a"));

		tree.put("cab", 2);
		Assertions.assertEquals(ImmutableSet.of(2), search(tree, "cab"));
		Assertions.assertEquals(ImmutableSet.of(2), search(tree, "ca"));
		Assertions.assertEquals(ImmutableSet.of(2), search(tree, "c"));
		Assertions.assertEquals(ImmutableSet.of(1, 2), search(tree, "ab"));
		Assertions.assertEquals(ImmutableSet.of(1, 2), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0, 1, 2), search(tree, "a"));

		tree.put("abcabxabcd", 3);
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcabxabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcabxabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcabxab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcabxa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcabx"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abca"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abc"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcabxabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcabxabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcabxab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcabxa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcabx"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bca"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bc"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "cabxabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "cabxabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "cabxab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "cabxa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "cabx"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abxabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abxabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abxab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abxa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abx"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bxabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bxabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bxab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bxa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bx"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "xabcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "xabc"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "xab"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "xa"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "x"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abcd"));
		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "abc"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "bcd"));

		Assertions.assertEquals(ImmutableSet.of(3), search(tree, "d"));

		Assertions.assertEquals(ImmutableSet.of(2, 3), search(tree, "cab"));
		Assertions.assertEquals(ImmutableSet.of(2, 3), search(tree, "ca"));
		Assertions.assertEquals(ImmutableSet.of(2, 3), search(tree, "c"));

		Assertions.assertEquals(ImmutableSet.of(1, 2, 3), search(tree, "ab"));
		Assertions.assertEquals(ImmutableSet.of(1, 2, 3), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0, 1, 2, 3), search(tree, "a"));
	}

	@Test
	public void testPuttingSameString() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "ab"));

		tree.put("ab", 1);
		Assertions.assertEquals(ImmutableSet.of(0, 1), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(0, 1), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0, 1), search(tree, "ab"));
	}

	@Test
	public void testPuttingShorterString() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "ab"));

		tree.put("a", 1);
		Assertions.assertEquals(ImmutableSet.of(0, 1), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(0), search(tree, "ab"));
	}

	@Test
	public void testNonMatchingSearches() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(ImmutableSet.of(), search(tree, ""));
		Assertions.assertEquals(ImmutableSet.of(), search(tree, "abc"));
		Assertions.assertEquals(ImmutableSet.of(), search(tree, "ac"));
		Assertions.assertEquals(ImmutableSet.of(), search(tree, "ba"));
		Assertions.assertEquals(ImmutableSet.of(), search(tree, "c"));
	}

	@Test
	public void testIndexWorksOutOfOrder() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 10);
		Assertions.assertEquals(ImmutableSet.of(10), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(10), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(10), search(tree, "ab"));

		tree.put("a", 5);
		Assertions.assertEquals(ImmutableSet.of(10, 5), search(tree, "a"));
		Assertions.assertEquals(ImmutableSet.of(10), search(tree, "b"));
		Assertions.assertEquals(ImmutableSet.of(10), search(tree, "ab"));
	}
}
