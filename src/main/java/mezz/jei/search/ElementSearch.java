package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IngredientFilterBackgroundBuilder;
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
	private final IngredientFilterBackgroundBuilder backgroundBuilder;
	private final CombinedSearchables combinedSearchables = new CombinedSearchables();
	/**
	 * indexed list of ingredients for use with the suffix trees
	 * includes all elements (even hidden ones) for use when rebuilding
	 */
	private final NonNullList<IIngredientListElementInfo<?>> elementInfoList;

	public ElementSearch() {
		this.elementInfoList = NonNullList.create();
		this.noPrefixSearchable = new GeneralizedSuffixTree();
		this.backgroundBuilder = new IngredientFilterBackgroundBuilder(prefixedSearchables, elementInfoList);
		this.combinedSearchables.addSearchable(noPrefixSearchable);
	}

	@Override
	public void start() {
		this.backgroundBuilder.start();
	}

	@Override
	public IntSet getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return IntSet.of();
		}
		final ISearchable searchable = this.prefixedSearchables.get(prefixInfo);
		if (searchable != null && searchable.getMode() != SearchMode.DISABLED) {
			return searchable.search(token);
		} else {
			return combinedSearchables.search(token);
		}
	}

	@Override
	public <V> void add(IIngredientListElementInfo<V> info) {
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
	public <V> IIngredientListElementInfo<V> get(int index) {
		return (IIngredientListElementInfo<V>) this.elementInfoList.get(index);
	}

	@Override
	public <V> int indexOf(IIngredientListElementInfo<V> ingredient) {
		return this.elementInfoList.indexOf(ingredient);
	}

	@Override
	public int size() {
		return this.elementInfoList.size();
	}

	@Override
	public List<IIngredientListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}

	@Override
	public void registerPrefix(PrefixInfo prefixInfo) {
		final GeneralizedSuffixTree searchable = new GeneralizedSuffixTree();
		final PrefixedSearchable<GeneralizedSuffixTree> prefixedSearchable = new PrefixedSearchable<>(searchable, prefixInfo);
		this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
		this.combinedSearchables.addSearchable(prefixedSearchable);
	}
}
