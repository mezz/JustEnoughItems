package mezz.jei.search;

import com.google.common.collect.ImmutableSet;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IIngredientListElementInfo;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchLowMem implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final NonNullList<IIngredientListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = NonNullList.create();
	}

	@Override
	public Set<IIngredientListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return ImmutableSet.of();
		}

		PrefixInfo<IIngredientListElementInfo<?>> prefixInfo = tokenInfo.prefixInfo();
		return this.elementInfoList.stream()
			.filter(elementInfo -> matches(token, prefixInfo, elementInfo))
			.collect(Collectors.toSet());
	}

	private static boolean matches(String word, PrefixInfo<IIngredientListElementInfo<?>> prefixInfo, IIngredientListElementInfo<?> elementInfo) {
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
	public void add(IIngredientListElementInfo<?> info) {
		this.elementInfoList.add(info);
	}

	@Override
	public void addAll(Collection<IIngredientListElementInfo<?>> infos) {
		this.elementInfoList.addAll(infos);
	}

	@Override
	public List<IIngredientListElementInfo<?>> getAllIngredients() {
		return Collections.unmodifiableList(this.elementInfoList);
	}

	@Override
	public void logStatistics() {
		LOGGER.info("ElementSearchLowMem Element Count: {}", this.elementInfoList.size());
	}
}
