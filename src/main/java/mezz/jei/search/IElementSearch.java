package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.ingredients.IIngredientListElementInfo;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface IElementSearch {
	<V> void add(IIngredientListElementInfo<V> info);

	<V> IIngredientListElementInfo<V> get(int index);

	<V> int indexOf(IIngredientListElementInfo<V> ingredient);

	int size();

	List<IIngredientListElementInfo<?>> getAllIngredients();

	@Nullable
	IntSet getSearchResults(String token, PrefixInfo prefixInfo);

	void registerPrefix(PrefixInfo prefixInfo);

	void start();
}
