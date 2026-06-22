package mezz.jei.gui.search;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.search.CombinedSearchables;
import mezz.jei.common.search.ISearchStorage;
import mezz.jei.common.search.ISearchable;
import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.search.PrefixedSearchable;
import mezz.jei.common.search.SearchMode;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.ListGroupElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo<IListElementInfo, IListElement>, PrefixedSearchable<IListElementInfo, IListElement>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElement> combinedSearchables = new CombinedSearchables<>();
	private final Map<Object, IListElement> allElements = new HashMap<>();

	public ElementSearch(ElementPrefixParser elementPrefixParser) {
		for (PrefixInfo<IListElementInfo, IListElement> prefixInfo : elementPrefixParser.allPrefixInfos()) {
			ISearchStorage<IListElement> storage = prefixInfo.createStorage();
			var prefixedSearchable = new PrefixedSearchable<>(storage, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IListElement> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return Set.of();
		}

		Set<IListElement> results = Collections.newSetFromMap(new IdentityHashMap<>());

		PrefixInfo<IListElementInfo, IListElement> prefixInfo = tokenInfo.prefixInfo();
		if (prefixInfo == ElementPrefixParser.NO_PREFIX) {
			combinedSearchables.getSearchResults(token, results::addAll);
			return results;
		}
		final ISearchable<IListElement> searchable = this.prefixedSearchables.get(prefixInfo);
		if (searchable == null || searchable.getMode() == SearchMode.DISABLED) {
			combinedSearchables.getSearchResults(token, results::addAll);
			return results;
		}
		searchable.getSearchResults(token, results::addAll);
		return results;
	}

	@Override
	public void add(IListElementInfo info, IIngredientManager ingredientManager) {
		IListElement element = info.getElement();
		Object uid = getUid(info, ingredientManager);
		this.allElements.put(uid, element);

		for (PrefixedSearchable<IListElementInfo, IListElement> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IListElement> storage = prefixedSearchable.getSearchStorage();
				for (String string : strings) {
					storage.put(string, element);
				}
			}
		}
	}

	private static Object getUid(IListElementInfo info, IIngredientManager ingredientManager) {
		if (info instanceof ListGroupElementInfo groupInfo) {
			return groupInfo.getGroupInfo().id();
		}
		return getIngredientUid(info.getTypedIngredient(), ingredientManager);
	}

	private static <T> Object getIngredientUid(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUid(typedIngredient.getIngredient(), UidContext.Ingredient);
	}

	@Override
	public void addAll(@UnknownNullability Collection<? extends IListElementInfo> infos, IIngredientManager ingredientManager) {
		for (IListElementInfo info : infos) {
			IListElement element = info.getElement();
			Object uid = getUid(info, ingredientManager);
			this.allElements.put(uid, element);
		}
		for (PrefixedSearchable<IListElementInfo, IListElement> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				ISearchStorage<IListElement> storage = prefixedSearchable.getSearchStorage();
				for (IListElementInfo info : infos) {
					Collection<String> strings = prefixedSearchable.getStrings(info);
					for (String string : strings) {
						storage.put(string, info.getElement());
					}
				}
			}
		}
	}

	@Override
	public @Nullable <T> IListElement findElement(ITypedIngredient<T> ingredient, IIngredientHelper<T> ingredientHelper) {
		Object ingredientUid = ingredientHelper.getUid(ingredient.getIngredient(), UidContext.Ingredient);
		IListElement listElement = allElements.get(ingredientUid);
		if (listElement != null && listElement.getTypedIngredient().getType().equals(ingredient.getType())) {
			return listElement;
		}
		return null;
	}

	@Override
	public Collection<IListElement> getAllIngredients() {
		return Collections.unmodifiableCollection(allElements.values());
	}

	@Override
	public void logStatistics() {
		this.prefixedSearchables.forEach((prefixInfo, value) -> {
			if (prefixInfo.getMode() != SearchMode.DISABLED) {
				ISearchStorage<IListElement> storage = value.getSearchStorage();
				LOGGER.info("ElementSearch {} Storage Stats: {}", prefixInfo, storage.statistics());
			}
		});
	}
}
