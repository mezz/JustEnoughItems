package mezz.jei.test.search.suffixtree;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class GeneralizedSuffixTreeTest {

	private static Set<Integer> search(GeneralizedSuffixTree<Integer> tree, String word) {
		IntSet results = new IntOpenHashSet();
		tree.getSearchResults(word, results);
		return results;
	}

	@Test
	public void testSearch() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("a", 0);
		Assertions.assertEquals(Set.of(0), search(tree, "a"));

		tree.put("ab", 1);
		Assertions.assertEquals(Set.of(1), search(tree, "ab"));
		Assertions.assertEquals(Set.of(1), search(tree, "b"));
		Assertions.assertEquals(Set.of(0, 1), search(tree, "a"));

		tree.put("cab", 2);
		Assertions.assertEquals(Set.of(2), search(tree, "cab"));
		Assertions.assertEquals(Set.of(2), search(tree, "ca"));
		Assertions.assertEquals(Set.of(2), search(tree, "c"));
		Assertions.assertEquals(Set.of(1, 2), search(tree, "ab"));
		Assertions.assertEquals(Set.of(1, 2), search(tree, "b"));
		Assertions.assertEquals(Set.of(0, 1, 2), search(tree, "a"));

		tree.put("abcabxabcd", 3);
		Assertions.assertEquals(Set.of(3), search(tree, "abcabxabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "abcabxabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "abcabxab"));
		Assertions.assertEquals(Set.of(3), search(tree, "abcabxa"));
		Assertions.assertEquals(Set.of(3), search(tree, "abcabx"));
		Assertions.assertEquals(Set.of(3), search(tree, "abcab"));
		Assertions.assertEquals(Set.of(3), search(tree, "abca"));
		Assertions.assertEquals(Set.of(3), search(tree, "abc"));

		Assertions.assertEquals(Set.of(3), search(tree, "bcabxabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "bcabxabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "bcabxab"));
		Assertions.assertEquals(Set.of(3), search(tree, "bcabxa"));
		Assertions.assertEquals(Set.of(3), search(tree, "bcabx"));
		Assertions.assertEquals(Set.of(3), search(tree, "bcab"));
		Assertions.assertEquals(Set.of(3), search(tree, "bca"));
		Assertions.assertEquals(Set.of(3), search(tree, "bc"));

		Assertions.assertEquals(Set.of(3), search(tree, "cabxabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "cabxabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "cabxab"));
		Assertions.assertEquals(Set.of(3), search(tree, "cabxa"));
		Assertions.assertEquals(Set.of(3), search(tree, "cabx"));

		Assertions.assertEquals(Set.of(3), search(tree, "abxabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "abxabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "abxab"));
		Assertions.assertEquals(Set.of(3), search(tree, "abxa"));
		Assertions.assertEquals(Set.of(3), search(tree, "abx"));

		Assertions.assertEquals(Set.of(3), search(tree, "bxabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "bxabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "bxab"));
		Assertions.assertEquals(Set.of(3), search(tree, "bxa"));
		Assertions.assertEquals(Set.of(3), search(tree, "bx"));

		Assertions.assertEquals(Set.of(3), search(tree, "xabcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "xabc"));
		Assertions.assertEquals(Set.of(3), search(tree, "xab"));
		Assertions.assertEquals(Set.of(3), search(tree, "xa"));
		Assertions.assertEquals(Set.of(3), search(tree, "x"));

		Assertions.assertEquals(Set.of(3), search(tree, "abcd"));
		Assertions.assertEquals(Set.of(3), search(tree, "abc"));

		Assertions.assertEquals(Set.of(3), search(tree, "bcd"));

		Assertions.assertEquals(Set.of(3), search(tree, "d"));

		Assertions.assertEquals(Set.of(2, 3), search(tree, "cab"));
		Assertions.assertEquals(Set.of(2, 3), search(tree, "ca"));
		Assertions.assertEquals(Set.of(2, 3), search(tree, "c"));

		Assertions.assertEquals(Set.of(1, 2, 3), search(tree, "ab"));
		Assertions.assertEquals(Set.of(1, 2, 3), search(tree, "b"));
		Assertions.assertEquals(Set.of(0, 1, 2, 3), search(tree, "a"));
	}

	@Test
	public void testPuttingSameString() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(Set.of(0), search(tree, "a"));
		Assertions.assertEquals(Set.of(0), search(tree, "b"));
		Assertions.assertEquals(Set.of(0), search(tree, "ab"));

		tree.put("ab", 1);
		Assertions.assertEquals(Set.of(0, 1), search(tree, "a"));
		Assertions.assertEquals(Set.of(0, 1), search(tree, "b"));
		Assertions.assertEquals(Set.of(0, 1), search(tree, "ab"));
	}

	@Test
	public void testPuttingShorterString() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(Set.of(0), search(tree, "a"));
		Assertions.assertEquals(Set.of(0), search(tree, "b"));
		Assertions.assertEquals(Set.of(0), search(tree, "ab"));

		tree.put("a", 1);
		Assertions.assertEquals(Set.of(0, 1), search(tree, "a"));
		Assertions.assertEquals(Set.of(0), search(tree, "b"));
		Assertions.assertEquals(Set.of(0), search(tree, "ab"));
	}

	@Test
	public void testNonMatchingSearches() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 0);
		Assertions.assertEquals(Set.of(), search(tree, ""));
		Assertions.assertEquals(Set.of(), search(tree, "abc"));
		Assertions.assertEquals(Set.of(), search(tree, "ac"));
		Assertions.assertEquals(Set.of(), search(tree, "ba"));
		Assertions.assertEquals(Set.of(), search(tree, "c"));
	}

	@Test
	public void testIndexWorksOutOfOrder() {
		GeneralizedSuffixTree<Integer> tree = new GeneralizedSuffixTree<>();

		tree.put("ab", 10);
		Assertions.assertEquals(Set.of(10), search(tree, "a"));
		Assertions.assertEquals(Set.of(10), search(tree, "b"));
		Assertions.assertEquals(Set.of(10), search(tree, "ab"));

		tree.put("a", 5);
		Assertions.assertEquals(Set.of(10, 5), search(tree, "a"));
		Assertions.assertEquals(Set.of(10), search(tree, "b"));
		Assertions.assertEquals(Set.of(10), search(tree, "ab"));
	}

//	@Test
//	public void testPrintedTree() {
//		GeneralizedSuffixTree<String> tree = new GeneralizedSuffixTree<>();
//		tree.put("java", "java");
//		tree.put("jei", "jei");
//		tree.put("test", "test");
//		tree.put("best", "best");
//		tree.put("bestest", "bestest");
//		tree.put("tester", "tester");
//		tree.put("er", "er");
//
//		FileWriter fileWriter = Assertions.assertDoesNotThrow(() -> new FileWriter("TestGeneralizedSuffixTree.dot"));
//		try (PrintWriter out = new PrintWriter(fileWriter)) {
//			tree.printTree(out, false);
//		}
//	}
}
