package mezz.jei.search;

import mezz.jei.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IListElementInfo<?> info);

	Collection<IListElementInfo<?>> getAllIngredients();

	Set<IListElementInfo<?>> getSearchResults(String token, PrefixInfo prefixInfo);

	void logStatistics();
}
