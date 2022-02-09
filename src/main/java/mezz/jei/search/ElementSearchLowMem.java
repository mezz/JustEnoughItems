package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IIngredientListElementInfo;
import net.minecraft.core.NonNullList;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ElementSearchLowMem implements IElementSearch {
	private final NonNullList<IIngredientListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = NonNullList.create();
	}

	@Nullable
	@Override
	public IntSet getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return null;
		}

		int[] results = IntStream.range(0, elementInfoList.size())
			.parallel()
			.filter(i -> {
				IIngredientListElementInfo<?> elementInfo = elementInfoList.get(i);
				return matches(token, prefixInfo, elementInfo);
			})
			.toArray();

		return new IntArraySet(results);
	}

	private static boolean matches(String word, PrefixInfo prefixInfo, IIngredientListElementInfo<?> elementInfo) {
		IIngredientListElement<?> element = elementInfo.getElement();
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
	public <V> void add(IIngredientListElementInfo<V> info) {
		this.elementInfoList.add(info);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> IIngredientListElementInfo<V> get(int index) {
		IIngredientListElementInfo<?> info = this.elementInfoList.get(index);
		return (IIngredientListElementInfo<V>) info;
	}

	@Override
	public <V> int indexOf(IIngredientListElementInfo<V> ingredient) {
		return this.elementInfoList.indexOf(ingredient);
	}

	@Override
	public int size() {
		return this.elementInfoList.size();
	}

	@Override
	public List<IIngredientListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}

	@Override
	public void start() {
		// noop
	}

	@Override
	public void registerPrefix(PrefixInfo prefixInfo) {
		// noop
	}
}
