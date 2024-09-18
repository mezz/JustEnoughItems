package mezz.jei.gui.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.Translator;
import mezz.jei.core.search.LimitedStringStorage;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.SearchMode;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class ElementPrefixParser {
	public static final PrefixInfo<IListElementInfo<?>, IListElement<?>> NO_PREFIX = new PrefixInfo<>(
		'\0',
		() -> SearchMode.ENABLED,
		IListElementInfo::getNames,
		GeneralizedSuffixTree::new
	);
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>, IListElement<?>>> map = new Char2ObjectOpenHashMap<>();

	public ElementPrefixParser(IIngredientManager ingredientManager, IIngredientFilterConfig config, IColorHelper colorHelper) {
		addPrefix(new PrefixInfo<>(
			'@',
			config::getModNameSearchMode,
			info -> {
				Set<String> modNames = new HashSet<>(info.getModNames());

				Set<String> sanitizedModNames = new HashSet<>();
				for (String modName : modNames) {
					modName = modName.toLowerCase();
					modName = SPACE_PATTERN.matcher(modName).replaceAll("");
					sanitizedModNames.add(modName);
				}

				return sanitizedModNames;
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

	private void addPrefix(PrefixInfo<IListElementInfo<?>, IListElement<?>> info) {
		this.map.put(info.getPrefix(), info);
	}

	public Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> allPrefixInfos() {
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> values = new ArrayList<>(map.values());
		values.add(NO_PREFIX);
		return values;
	}

	public record TokenInfo(String token, PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo) {}

	public Optional<TokenInfo> parseToken(String token) {
		if (token.isEmpty()) {
			return Optional.empty();
		}
		char firstChar = token.charAt(0);
		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = map.get(firstChar);
		if (prefixInfo == null || prefixInfo.getMode() == SearchMode.DISABLED) {
			return Optional.of(new TokenInfo(token, NO_PREFIX));
		}
		if (token.length() == 1) {
			return Optional.empty();
		}
		return Optional.of(new TokenInfo(token.substring(1), prefixInfo));
	}
}
