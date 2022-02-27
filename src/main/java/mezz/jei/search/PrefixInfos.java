package mezz.jei.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PrefixInfos {
	private final Char2ObjectMap<PrefixInfo> map = new Char2ObjectOpenHashMap<>();

	public PrefixInfos(RegisteredIngredients registeredIngredients, IIngredientFilterConfig config) {
		addPrefix(new PrefixInfo(
			'@',
			config::getModNameSearchMode,
			IListElementInfo::getModNameStrings,
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo(
			'#',
			config::getTooltipSearchMode,
			e -> e.getTooltipStrings(config, registeredIngredients),
			GeneralizedSuffixTree::new
		));
		addPrefix(new PrefixInfo(
			'$',
			config::getTagSearchMode,
			e -> e.getTagStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo(
			'%',
			config::getCreativeTabSearchMode,
			e -> e.getCreativeTabsStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo(
			'^',
			config::getColorSearchMode,
			e -> e.getColorStrings(registeredIngredients),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo(
			'&',
			config::getResourceLocationSearchMode,
			element -> List.of(element.getResourceLocation().toString()),
			GeneralizedSuffixTree::new
		));
	}

	private void addPrefix(PrefixInfo info) {
		this.map.put(info.getPrefix(), info);
	}

	public Collection<PrefixInfo> allPrefixInfos() {
		Collection<PrefixInfo> values = new ArrayList<>(map.values());
		values.add(PrefixInfo.NO_PREFIX);
		return values;
	}

	public Pair<String, PrefixInfo> parseToken(String token) {
		if (token.isEmpty()) {
			return new Pair<>(token, PrefixInfo.NO_PREFIX);
		}
		char firstChar = token.charAt(0);
		PrefixInfo prefixInfo = map.get(firstChar);
		if (prefixInfo == null || prefixInfo.getMode() == SearchMode.DISABLED) {
			return new Pair<>(token, PrefixInfo.NO_PREFIX);
		}
		return new Pair<>(token.substring(1), prefixInfo);
	}
}
