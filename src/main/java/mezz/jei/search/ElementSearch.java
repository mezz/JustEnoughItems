package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.ingredients.PrefixedSearchable;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import net.minecraft.core.NonNullList;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ElementSearch implements IElementSearch {
	private final GeneralizedSuffixTree noPrefixSearchable;
	private final Map<PrefixInfo, PrefixedSearchable<GeneralizedSuffixTree>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables combinedSearchables = new CombinedSearchables();
	/**
	 * indexed list of ingredients for use with the suffix trees
	 * includes all elements (even hidden ones) for use when rebuilding
	 */
	private final NonNullList<IListElementInfo<?>> elementInfoList;

	public ElementSearch(PrefixInfos prefixInfos) {
		this.elementInfoList = NonNullList.create();
		this.noPrefixSearchable = new GeneralizedSuffixTree();
		this.combinedSearchables.addSearchable(noPrefixSearchable);
		for (PrefixInfo prefixInfo : prefixInfos.values()) {
			final GeneralizedSuffixTree searchable = new GeneralizedSuffixTree();
			final PrefixedSearchable<GeneralizedSuffixTree> prefixedSearchable = new PrefixedSearchable<>(searchable, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public IntSet getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return IntSet.of();
		}

		final ISearchable searchable = this.prefixedSearchables.get(prefixInfo);

		IntSet results = new IntOpenHashSet(1000);
		if (searchable != null && searchable.getMode() != SearchMode.DISABLED) {
			searchable.addSearchResults(token, results);
		} else {
			combinedSearchables.addSearchResults(token, results);
		}
		return results;
	}

	@Override
	public <V> void add(IListElementInfo<V> info) {
		int index = this.elementInfoList.size();
		this.elementInfoList.add(info);

		{
			Collection<String> strings = PrefixInfo.NO_PREFIX.getStrings(info);
			for (String string : strings) {
				this.noPrefixSearchable.put(string, index);
			}
		}

		for (PrefixedSearchable<GeneralizedSuffixTree> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				GeneralizedSuffixTree searchable = prefixedSearchable.getSearchable();
				for (String string : strings) {
					searchable.put(string, index);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> IListElementInfo<V> get(int index) {
		return (IListElementInfo<V>) this.elementInfoList.get(index);
	}

	@Override
	public <V> int indexOf(IListElementInfo<V> ingredient) {
		return this.elementInfoList.indexOf(ingredient);
	}

	@Override
	public int size() {
		return this.elementInfoList.size();
	}

	@Override
	public List<IListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}
}
