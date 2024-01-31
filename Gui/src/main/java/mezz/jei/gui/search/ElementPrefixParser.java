package mezz.jei.gui.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.Translator;
import mezz.jei.core.search.LimitedStringStorage;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.SearchMode;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ElementPrefixParser {
	public static final PrefixInfo<IListElementInfo<?>> NO_PREFIX = new PrefixInfo<>(
			'\0',
			() -> SearchMode.ENABLED,
			i -> List.of(i.getName()),
			GeneralizedSuffixTree::new
	);
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
	private static final Pattern MOD_NAME_SEPARATOR_PATTERN = Pattern.compile("(?=[A-Z_-])|\\s+");

	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>>> map = new Char2ObjectOpenHashMap<>();

	public ElementPrefixParser(IIngredientManager ingredientManager, IIngredientFilterConfig config, IColorHelper colorHelper, IModIdHelper modIdHelper) {
		addPrefix(new PrefixInfo<>(
			'@',
			config::getModNameSearchMode,
			info -> {
				Stream<String> modNames = info.getModNames()
					.stream();

				if (config.getSearchModIds()) {
					Stream<String> modIds = info.getModIds()
						.stream();

					modNames = Stream.concat(modNames, modIds);
				}

				if (config.getSearchModAliases()) {
					Stream<String> modAliases = info.getModIds()
						.stream()
						.map(modIdHelper::getModAliases)
						.flatMap(Collection::stream);

					modNames = Stream.concat(modNames, modAliases);
				}

				if (config.getSearchShortModNames()) {
					Stream<String> shortModNames = info.getModNames()
						.stream()
						.flatMap(ElementPrefixParser::getShortModNames);

					modNames = Stream.concat(modNames, shortModNames);
				}

				return modNames
					.map(String::toLowerCase)
					.map(SPACE_PATTERN::matcher)
					.map(matcher -> matcher.replaceAll(""))
					.distinct()
					.toList();
			},
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
				Iterable<Integer> colors = e.getColors(ingredientManager);
				return StreamSupport.stream(colors.spliterator(), false)
					.map(colorHelper::getClosestColorName)
					.map(Translator::toLowercaseWithLocale)
					.distinct()
					.toList();
			},
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

	private static Stream<String> getShortModNames(String modName) {
		String[] words = MOD_NAME_SEPARATOR_PATTERN.split(modName);
		if (words.length <= 1) {
			return Stream.empty();
		}
		return Stream.of(
			combineFirstLetters(words, 1),
			combineFirstLetters(words, 2)
		);
	}

	private static String combineFirstLetters(String[] words, final int count){
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			int end = Math.min(count, word.length());
			sb.append(word, 0, end);
		}
		return sb.toString();
	}
}
