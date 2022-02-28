package mezz.jei.search;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.util.Translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ElementPrefixParser {
	public static final PrefixInfo<IIngredientListElementInfo<?>> NO_PREFIX = new PrefixInfo<>(
			'\0',
			() -> SearchMode.ENABLED,
			i -> ImmutableList.of(i.getName()),
			GeneralizedSuffixTree::new
	);

	private final Char2ObjectMap<PrefixInfo<IIngredientListElementInfo<?>>> map = new Char2ObjectOpenHashMap<>();

	public ElementPrefixParser(IIngredientManager ingredientManager, IIngredientFilterConfig config) {
		addPrefix(new PrefixInfo<>(
			'@',
			config::getModNameSearchMode,
			IIngredientListElementInfo::getModNameStrings,
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'#',
			config::getTooltipSearchMode,
			e -> e.getTooltipStrings(config, ingredientManager),
			GeneralizedSuffixTree::new
		));
		addPrefix(new PrefixInfo<>(
			'$',
			config::getTagSearchMode,
			e -> e.getTagStrings(ingredientManager),
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'^',
			config::getColorSearchMode,
			e -> {
				Iterable<String> colors = e.getColorStrings(ingredientManager);
				return StreamSupport.stream(colors.spliterator(), false)
					.map(Translator::toLowercaseWithLocale)
					.distinct()
					.collect(Collectors.toList());
			},
			LimitedStringStorage::new
		));
		addPrefix(new PrefixInfo<>(
			'&',
			config::getResourceIdSearchMode,
			element -> ImmutableList.of(element.getResourceId()),
			GeneralizedSuffixTree::new
		));
	}

	private void addPrefix(PrefixInfo<IIngredientListElementInfo<?>> info) {
		this.map.put(info.getPrefix(), info);
	}

	public Collection<PrefixInfo<IIngredientListElementInfo<?>>> allPrefixInfos() {
		Collection<PrefixInfo<IIngredientListElementInfo<?>>> values = new ArrayList<>(map.values());
		values.add(NO_PREFIX);
		return values;
	}

	public static final class TokenInfo {
		private final String token;
		private final PrefixInfo<IIngredientListElementInfo<?>> prefixInfo;

		public TokenInfo(String token, PrefixInfo<IIngredientListElementInfo<?>> prefixInfo) {
			this.token = token;
			this.prefixInfo = prefixInfo;
		}

		public String token() {
			return token;
		}

		public PrefixInfo<IIngredientListElementInfo<?>> prefixInfo() {
			return prefixInfo;
		}
	}

	public Optional<TokenInfo> parseToken(String token) {
		if (token.isEmpty()) {
			return Optional.empty();
		}
		char firstChar = token.charAt(0);
		PrefixInfo<IIngredientListElementInfo<?>> prefixInfo = map.get(firstChar);
		if (prefixInfo == null || prefixInfo.getMode() == SearchMode.DISABLED) {
			return Optional.of(new TokenInfo(token, NO_PREFIX));
		}
		if (token.length() == 1) {
			return Optional.empty();
		}
		return Optional.of(new TokenInfo(token.substring(1), prefixInfo));
	}
}
