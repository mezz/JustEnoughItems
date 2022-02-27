package mezz.jei.search;

import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.ingredients.IListElementInfo;
import net.minecraft.core.NonNullList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchLowMem implements IElementSearch {
	private final NonNullList<IListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = NonNullList.create();
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return Set.of();
		}

		return this.elementInfoList.stream()
			.filter(elementInfo -> matches(token, prefixInfo, elementInfo))
			.collect(Collectors.toSet());
	}

	private static boolean matches(String word, PrefixInfo prefixInfo, IListElementInfo<?> elementInfo) {
		IListElement<?> element = elementInfo.getElement();
		if (element.isVisible()) {
			Collection<String> strings = prefixInfo.getStrings(elementInfo);
			for (String string : strings) {
				if (string.contains(word)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void add(IListElementInfo<?> info) {
		this.elementInfoList.add(info);
	}

	@Override
	public List<IListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}
}
