package mezz.jei.gui.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.Translator;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.config.SearchMode;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class ElementPrefixParser {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
	private static final Pattern MOD_NAME_SEPARATOR_PATTERN = Pattern.compile("(?=[A-Z_-])|\\s+");

	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>, IListElement<?>>> map = new Char2ObjectLinkedOpenHashMap<>();
	private final Map<ElementSearchIndex, PrefixInfo<IListElementInfo<?>, IListElement<?>>> prefixInfoBySearchIndex = new EnumMap<>(ElementSearchIndex.class);
	private final PrefixInfo<IListElementInfo<?>, IListElement<?>> noPrefix;

	public ElementPrefixParser(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig config,
		IColorHelper colorHelper,
		IModIdHelper modIdHelper,
		ISearchStorageBuilderFactory searchStorageBuilderFactory
	) {
		ISearchStorageBuilderFactory limitedStringStorageBuilderFactory = createLimitedStringStorageBuilderFactory(searchStorageBuilderFactory);

		this.noPrefix = new PrefixInfo<>(
			"unprefixed",
			'\0',
			Component.empty(),
			true,
			() -> SearchMode.ENABLED,
			IListElementInfo::getNames,
			searchStorageBuilderFactory
		);
		this.prefixInfoBySearchIndex.put(ElementSearchIndex.NAMES_AND_ALIASES, noPrefix);

		addPrefix(ElementSearchIndex.MOD_NAMES, new PrefixInfo<>(
			"mod_names",
			'@',
			Component.translatable("jei.search.completion.prefix.modName"),
			true,
			config::getModNameSearchMode,
			info -> {
				Set<String> modNames = new HashSet<>(info.getModNames());

				if (config.getSearchModIds()) {
					modNames.addAll(info.getModIds());
				}

				if (config.getSearchModAliases()) {
					for (String modId : info.getModIds()) {
						modNames.addAll(modIdHelper.getModAliases(modId));
					}
				}

				if (config.getSearchShortModNames()) {
					for (String modName : info.getModNames()) {
						List<String> shortModNames = getShortModNames(modName);
						modNames.addAll(shortModNames);
					}
				}

				Set<String> sanitizedModNames = new HashSet<>();
				for (String modName : modNames) {
					modName = modName.toLowerCase();
					modName = SPACE_PATTERN.matcher(modName).replaceAll("");
					sanitizedModNames.add(modName);
				}

				return sanitizedModNames;
			},
			limitedStringStorageBuilderFactory
		));
		addPrefix(ElementSearchIndex.TAGS, new PrefixInfo<>(
			"tags",
			'#',
			Component.translatable("jei.search.completion.prefix.tag"),
			true,
			config::getTagSearchMode,
			e -> e.getTagStrings(ingredientManager),
			limitedStringStorageBuilderFactory
		));
		addPrefix(ElementSearchIndex.TOOLTIPS, new PrefixInfo<>(
			"tooltips",
			'$',
			Component.translatable("jei.search.completion.prefix.tooltip"),
			false,
			config::getTooltipSearchMode,
			e -> e.getTooltipStrings(config, ingredientManager),
			searchStorageBuilderFactory
		));
		addPrefix(ElementSearchIndex.CREATIVE_TABS, new PrefixInfo<>(
			"creative_tabs",
			'%',
			Component.translatable("jei.search.completion.prefix.creativeTab"),
			true,
			config::getCreativeTabSearchMode,
			e -> e.getCreativeTabsStrings(ingredientManager),
			limitedStringStorageBuilderFactory
		));
		addPrefix(ElementSearchIndex.COLORS, new PrefixInfo<>(
			"colors",
			'^',
			Component.translatable("jei.search.completion.prefix.color"),
			true,
			config::getColorSearchMode,
			e -> {
				Iterable<Integer> colors = e.getColors(ingredientManager);
				return StreamSupport.stream(colors.spliterator(), false)
					.map(colorHelper::getClosestColorName)
					.map(Translator::toLowercaseWithLocale)
					.distinct()
					.toList();
			},
			limitedStringStorageBuilderFactory
		));
		addPrefix(ElementSearchIndex.IDENTIFIERS, new PrefixInfo<>(
			"identifiers",
			'&',
			Component.translatable("jei.search.completion.prefix.identifier"),
			true,
			config::getResourceLocationSearchMode,
			element -> List.of(element.getResourceLocation().toString()),
			searchStorageBuilderFactory
		));
	}

	private static ISearchStorageBuilderFactory createLimitedStringStorageBuilderFactory(
		ISearchStorageBuilderFactory searchStorageBuilderFactory
	) {
		return new ISearchStorageBuilderFactory() {
			@Override
			public <T> LimitedStringStorageBuilder<T> create() {
				return new LimitedStringStorageBuilder<>(searchStorageBuilderFactory);
			}

			@Override
			public <T> LimitedStringStorageBuilder<T> create(String id) {
				return new LimitedStringStorageBuilder<>(searchStorageBuilderFactory, id);
			}

			@Override
			public boolean supportsDeletion() {
				return searchStorageBuilderFactory.supportsDeletion();
			}
		};
	}

	private void addPrefix(ElementSearchIndex searchIndex, PrefixInfo<IListElementInfo<?>, IListElement<?>> info) {
		this.map.put(info.getPrefix(), info);
		this.prefixInfoBySearchIndex.put(searchIndex, info);
	}

	PrefixInfo<IListElementInfo<?>, IListElement<?>> getPrefixInfo(ElementSearchIndex searchIndex) {
		return prefixInfoBySearchIndex.get(searchIndex);
	}

	public Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> allPrefixInfos() {
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> values = new ArrayList<>();
		values.add(noPrefix);
		values.addAll(map.values());
		return values;
	}

	public PrefixInfo<IListElementInfo<?>, IListElement<?>> getNoPrefix() {
		return noPrefix;
	}

	public record TokenInfo(String token, PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo) {}

	public Optional<TokenInfo> parseToken(String token) {
		if (token.isEmpty()) {
			return Optional.empty();
		}
		char firstChar = token.charAt(0);
		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = map.get(firstChar);
		if (prefixInfo == null || prefixInfo.getMode() == SearchMode.DISABLED) {
			return Optional.of(new TokenInfo(token, noPrefix));
		}
		if (token.length() == 1) {
			return Optional.empty();
		}
		return Optional.of(new TokenInfo(token.substring(1), prefixInfo));
	}

	private static List<String> getShortModNames(String modName) {
		String[] words = MOD_NAME_SEPARATOR_PATTERN.split(modName);
		if (words.length <= 1) {
			return List.of();
		}
		return List.of(
			combineFirstLetters(words, 1),
			combineFirstLetters(words, 2)
		);
	}

	private static String combineFirstLetters(String[] words, final int count) {
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			int end = Math.min(count, word.length());
			sb.append(word, 0, end);
		}
		return sb.toString();
	}
}
