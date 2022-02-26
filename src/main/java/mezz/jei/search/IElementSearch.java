package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.ingredients.IListElementInfo;

import java.util.List;

public interface IElementSearch {
	<V> void add(IListElementInfo<V> info);

	<V> IListElementInfo<V> get(int index);

	<V> int indexOf(IListElementInfo<V> ingredient);

	int size();

	List<IListElementInfo<?>> getAllIngredients();

	IntSet getSearchResults(String token, PrefixInfo prefixInfo);
}
