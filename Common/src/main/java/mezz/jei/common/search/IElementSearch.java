package mezz.jei.common.search;

import mezz.jei.common.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IListElementInfo<?> info);

	Collection<IListElementInfo<?>> getAllIngredients();

	Set<IListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	@SuppressWarnings("unused") // used for debugging
	void logStatistics();
}
