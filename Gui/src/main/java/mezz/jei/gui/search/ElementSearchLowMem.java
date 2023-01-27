package mezz.jei.gui.search;

import mezz.jei.api.search.ILanguageTransformer;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchLowMem implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IListElementInfo<?>> elementInfoList;
	@Unmodifiable
	private final List<ILanguageTransformer> languageTransformers;

	public ElementSearchLowMem(Collection<ILanguageTransformer> languageTransformers) {
		this.languageTransformers = List.copyOf(languageTransformers);
		this.elementInfoList = new ArrayList<>();
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = transform(tokenInfo.token());
		if (token.isEmpty()) {
			return Set.of();
		}

		PrefixInfo<IListElementInfo<?>> prefixInfo = tokenInfo.prefixInfo();
		return this.elementInfoList.stream()
			.filter(elementInfo -> matches(token, prefixInfo, elementInfo))
			.collect(Collectors.toSet());
	}

	private String transform(String string) {
		for (ILanguageTransformer languageTransformer : languageTransformers) {
			string = languageTransformer.transformToken(string);
		}
		return string;
	}

	private boolean matches(String word, PrefixInfo<IListElementInfo<?>> prefixInfo, IListElementInfo<?> elementInfo) {
		IListElement<?> element = elementInfo.getElement();
		if (element.isVisible()) {
			Collection<String> strings = prefixInfo.getStrings(elementInfo);
			for (String string : strings) {
				string = transform(string);
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
