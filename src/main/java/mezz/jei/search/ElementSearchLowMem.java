package mezz.jei.search;

import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.ingredients.IListElementInfo;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchLowMem implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final NonNullList<IListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = NonNullList.create();
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(PrefixInfos.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return Set.of();
		}

		PrefixInfo prefixInfo = tokenInfo.prefixInfo();
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

	@Override
	public void logStatistics() {
		LOGGER.info("ElementSearchLowMem Element Count: {}", this.elementInfoList.size());
	}
}
