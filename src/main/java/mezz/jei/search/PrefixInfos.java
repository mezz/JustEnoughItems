package mezz.jei.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.util.Pair;

import java.util.Collection;
import java.util.List;

public class PrefixInfos {
	private final Char2ObjectMap<PrefixInfo> map = new Char2ObjectOpenHashMap<>();

	public PrefixInfos(RegisteredIngredients registeredIngredients, IIngredientFilterConfig config) {
		this.map.put('@', new PrefixInfo(config::getModNameSearchMode, IListElementInfo::getModNameStrings));
		this.map.put('#', new PrefixInfo(config::getTooltipSearchMode, e -> e.getTooltipStrings(config, registeredIngredients)));
		this.map.put('$', new PrefixInfo(config::getTagSearchMode, e -> e.getTagStrings(registeredIngredients)));
		this.map.put('%', new PrefixInfo(config::getCreativeTabSearchMode, e -> e.getCreativeTabsStrings(registeredIngredients)));
		this.map.put('^', new PrefixInfo(config::getColorSearchMode, e -> e.getColorStrings(registeredIngredients)));
		this.map.put('&', new PrefixInfo(config::getResourceLocationSearchMode, element -> List.of(element.getResourceLocation().toString())));
	}

	public Collection<PrefixInfo> values() {
		return map.values();
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
