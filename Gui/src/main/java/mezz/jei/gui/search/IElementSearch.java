package mezz.jei.gui.search;

import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IListElementInfo<?> info);

	void addAll(Collection<IListElementInfo<?>> infos);

	Collection<IListElement<?>> getAllIngredients();

	Set<IListElement<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	void logStatistics();
}
