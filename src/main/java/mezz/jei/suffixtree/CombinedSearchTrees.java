package mezz.jei.suffixtree;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class CombinedSearchTrees implements ISearchTree {
	private final List<ISearchTree> searchTrees = new ArrayList<>();

	@Override
	public TIntSet search(String word) {
		TIntSet searchResults = new TIntHashSet(0);
		for (ISearchTree searchTree : searchTrees) {
			TIntSet search = searchTree.search(word);
			searchResults = union(searchResults, search);
		}
		return searchResults;
	}

	public void addSearchTree(ISearchTree searchTree) {
		this.searchTrees.add(searchTree);
	}

	/**
	 * Efficiently get all the elements from both sets.
	 * Note that this implementation will alter the original sets.
	 */
	private static TIntSet union(TIntSet set1, TIntSet set2) {
		if (set1.size() > set2.size()) {
			set1.addAll(set2);
			return set1;
		} else {
			set2.addAll(set1);
			return set2;
		}
	}
}
