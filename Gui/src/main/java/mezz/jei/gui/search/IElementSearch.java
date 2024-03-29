package mezz.jei.gui.search;

import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IListElementInfo<?> info);

	void addAll(Collection<IListElementInfo<?>> infos);

	Collection<IListElementInfo<?>> getAllIngredients();

	Set<IListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	@SuppressWarnings("unused") // used for debugging
	void logStatistics();
}
