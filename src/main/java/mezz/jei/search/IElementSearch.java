package mezz.jei.search;

import mezz.jei.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IListElementInfo<?> info);

	Collection<IListElementInfo<?>> getAllIngredients();

	Set<IListElementInfo<?>> getSearchResults(PrefixInfos.TokenInfo tokenInfo);

	@SuppressWarnings("unused") // used for debugging
	void logStatistics();
}
