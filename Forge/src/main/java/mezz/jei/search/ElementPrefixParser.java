package mezz.jei.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.core.search.LimitedStringStorage;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ElementPrefixParser {
	public static final PrefixInfo<IListElementInfo<?>> NO_PREFIX = new PrefixInfo<>(
			'\0',
			() -> SearchMode.ENABLED,
			i -> List.of(i.getName()),
			GeneralizedSuffixTree::new
	);

	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>>> map = new Char2ObjectOpenHashMap<>();

	public ElementPrefixParser(RegisteredIngredients registeredIngredients, IIngredientFilterConfig config) {
		addPrefix(new PrefixInfo<>(
			'@',
			config::getModNameSearchMode,
			IListElementInfo::getModNameStrings,
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'#',
			config::getTooltipSearchMode,
			e -> e.getTooltipStrings(config, registeredIngredients),
			GeneralizedSuffixTree::new
		));
		addPrefix(new PrefixInfo<>(
			'$',
			config::getTagSearchMode,
			e -> e.getTagStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'%',
			config::getCreativeTabSearchMode,
			e -> e.getCreativeTabsStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'^',
			config::getColorSearchMode,
			e -> e.getColorStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'&',
			config::getResourceLocationSearchMode,
			element -> List.of(element.getResourceLocation().toString()),
			GeneralizedSuffixTree::new
		));
	}

	private void addPrefix(PrefixInfo<IListElementInfo<?>> info) {
		this.map.put(info.getPrefix(), info);
	}

	public Collection<PrefixInfo<IListElementInfo<?>>> allPrefixInfos() {
		Collection<PrefixInfo<IListElementInfo<?>>> values = new ArrayList<>(map.values());
		values.add(NO_PREFIX);
		return values;
	}

	public record TokenInfo(String token, PrefixInfo<IListElementInfo<?>> prefixInfo) {}

	public Optional<TokenInfo> parseToken(String token) {
		if (token.isEmpty()) {
			return Optional.empty();
		}
		char firstChar = token.charAt(0);
		PrefixInfo<IListElementInfo<?>> prefixInfo = map.get(firstChar);
		if (prefixInfo == null || prefixInfo.getMode() == SearchMode.DISABLED) {
			return Optional.of(new TokenInfo(token, NO_PREFIX));
		}
		if (token.length() == 1) {
			return Optional.empty();
		}
		return Optional.of(new TokenInfo(token.substring(1), prefixInfo));
	}
}
