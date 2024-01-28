package mezz.jei.search;

import mezz.jei.ingredients.IIngredientListElementInfo;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	void add(IIngredientListElementInfo<?> info);

	void addAll(Collection<IIngredientListElementInfo<?>> infos);

	Collection<IIngredientListElementInfo<?>> getAllIngredients();

	Set<IIngredientListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	@SuppressWarnings("unused") // used for debugging
	void logStatistics();
}