package mezz.jei.gui.search;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface IElementSearch {
	<T> void add(IListElementInfo<T> info, IIngredientManager ingredientManager);

	Collection<IListElement<?>> getAllIngredients();

	void update(
		Collection<IListElementInfo<?>> updatedInfos,
		Set<ElementSearchIndex> rebuildingIndexes,
		Collection<IListElementInfo<?>> allInfos
	);

	void rebuildSearchIndexes(
		Set<ElementSearchIndex> searchIndexes,
		Collection<IListElementInfo<?>> elementInfos
	);

	Set<IListElement<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	<T> Optional<IListElement<T>> findElement(ITypedIngredient<T> ingredient, IIngredientHelper<T> ingredientHelper);

	void logStatistics();
}
