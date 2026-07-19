package mezz.jei.gui.search;

import mezz.jei.common.search.PrefixInfo;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;

public interface ISearchCompletionProvider {

	Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> getAllPrefixInfos();

	Collection<IListElementInfo<?>> getAllElementInfos();
}
