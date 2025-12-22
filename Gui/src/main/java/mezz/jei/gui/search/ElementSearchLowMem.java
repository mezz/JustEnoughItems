package mezz.jei.gui.search;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.gui.ingredients.DisplayNameUtil;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElementSearchLowMem implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IListElementInfo<?>> elementInfoList;

	public ElementSearchLowMem() {
		this.elementInfoList = new ArrayList<>();
	}

	@Override
	public Set<IListElement<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return Set.of();
		}

		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = tokenInfo.prefixInfo();
		return this.elementInfoList.stream()
			.filter(elementInfo -> matches(token, prefixInfo, elementInfo))
			.map(IListElementInfo::getElement)
			.collect(Collectors.toSet());
	}

	private static boolean matches(String word, PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo, IListElementInfo<?> elementInfo) {
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
	public <T> void add(IListElementInfo<T> info, IIngredientManager ingredientManager) {
		this.elementInfoList.add(info);
	}

	@Override
	public void addAll(Collection<IListElementInfo<?>> infos, IIngredientManager ingredientManager) {
		this.elementInfoList.addAll(infos);
	}

	@Override
	public List<IListElement<?>> getAllIngredients() {
		return Lists.transform(this.elementInfoList, IListElementInfo::getElement);
	}

	@Override
	public @Nullable <T> IListElement<T> findElement(ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		T ingredient = typedIngredient.getIngredient();
		IIngredientType<T> type = typedIngredient.getType();
		Function<ITypedIngredient<T>, Object> uidFunction = (i) -> ingredientHelper.getUid(i, UidContext.Ingredient);
		Object ingredientUid = uidFunction.apply(typedIngredient);
		String lowercaseDisplayName = DisplayNameUtil.getLowercaseDisplayNameForSearch(ingredient, ingredientHelper);

		ElementPrefixParser.TokenInfo tokenInfo = new ElementPrefixParser.TokenInfo(lowercaseDisplayName, ElementPrefixParser.NO_PREFIX);
		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = tokenInfo.prefixInfo();

		for (IListElementInfo<?> elementInfo : this.elementInfoList) {
			if (matches(lowercaseDisplayName, prefixInfo, elementInfo)) {
				IListElement<?> element = elementInfo.getElement();
				IListElement<T> match = checkForMatch(element, type, ingredientUid, uidFunction);
				if (match != null) {
					return match;
				}
			}
		}

		return null;
	}

	@Nullable
	private static <T> IListElement<T> checkForMatch(IListElement<?> element, IIngredientType<T> ingredientType, Object uid, Function<ITypedIngredient<T>, Object> uidFunction) {
		IListElement<T> cast = optionalCast(element, ingredientType);
		if (cast == null) {
			return null;
		}
		ITypedIngredient<T> typedIngredient = cast.getTypedIngredient();
		Object elementUid = uidFunction.apply(typedIngredient);
		if (uid.equals(elementUid)) {
			return cast;
		}
		return null;
	}

	@Nullable
	private static <T> IListElement<T> optionalCast(IListElement<?> element, IIngredientType<T> ingredientType) {
		ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
		if (typedIngredient.getType() == ingredientType) {
			@SuppressWarnings("unchecked")
			IListElement<T> cast = (IListElement<T>) element;
			return cast;
		}
		return null;
	}

	@Override
	public void logStatistics() {
		LOGGER.info("ElementSearchLowMem Element Count: {}", this.elementInfoList.size());
	}
}
