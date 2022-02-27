package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.ingredients.IListElementInfo;
import net.minecraft.core.NonNullList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ElementSearchLowMem implements IElementSearch {
	private final NonNullList<IListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = NonNullList.create();
	}

	@Override
	public IntSet getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return IntSet.of();
		}

		int[] results = IntStream.range(0, elementInfoList.size())
			.parallel()
			.filter(i -> {
				IListElementInfo<?> elementInfo = elementInfoList.get(i);
				return matches(token, prefixInfo, elementInfo);
			})
			.toArray();

		return new IntArraySet(results);
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
	public <V> void add(IListElementInfo<V> info) {
		this.elementInfoList.add(info);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> IListElementInfo<V> get(int index) {
		IListElementInfo<?> info = this.elementInfoList.get(index);
		return (IListElementInfo<V>) info;
	}

	@Override
	public <V> int indexOf(IListElementInfo<V> ingredient) {
		return this.elementInfoList.indexOf(ingredient);
	}

	@Override
	public int size() {
		return this.elementInfoList.size();
	}

	@Override
	public List<IListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}

	@Override
	public void registerPrefix(PrefixInfo prefixInfo) {
		// noop
	}
}
