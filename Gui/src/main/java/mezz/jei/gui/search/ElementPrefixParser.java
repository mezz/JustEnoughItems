package mezz.jei.gui.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.Translator;
import mezz.jei.core.search.LimitedStringStorage;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.SearchMode;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

public class ElementPrefixParser {
	public static final PrefixInfo<IListElementInfo<?>> NO_PREFIX = new PrefixInfo<>(
			'\0',
			() -> SearchMode.ENABLED,
			i -> List.of(i.getName()),
			GeneralizedSuffixTree::new
	);

	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>>> map = new Char2ObjectOpenHashMap<>();

	public ElementPrefixParser(IIngredientManager ingredientManager, IIngredientFilterConfig config, IColorHelper colorHelper, IModIdHelper modIdHelper) {
		addPrefix(new PrefixInfo<>(
			'@',
			config::getModNameSearchMode,
			info -> {
                List<String> modNames = info.getModNameStrings()
						.stream()
						.map(modIdHelper::getModNameForModId)
						.map(ElementPrefixParser::convertModNameToSortId)
						.flatMap(Set::stream)
						.toList();
                Set<String> modIdAlias = new HashSet<>(modNames);
				modIdAlias.addAll(info.getModNameStrings());
				modIdAlias.addAll(modIdHelper.getModAliases(info.getResourceLocation().getNamespace()));
                return modIdAlias;
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

	private static Set<String> convertModNameToSortId(String modName) {
		//spilt modName by UpperCase or _ or -.
		List<String> words = new ArrayList<>(List.of(modName.split("(?=[A-Z_-])|\\s+")));
        //if modName only have one word, we can't find its shortened form.
		if (words.size() <= 1) return Collections.emptySet();
		words.removeIf(s -> s.isEmpty() || s.isBlank() || s.equals("-") || s.equals("_"));

		Set<String> sortIds = new HashSet<>();
        sortIds.add(pickAndCombine(words, 1));
        //some mod name only have two words, so they may have a special sortId,such as crafttweaker -> crt, Tinkers' Construct -> tic.
		//For mods with single word names like Mekanism, we currently have no good way to find its shortened form.
        if (words.size() == 2) sortIds.add(pickAndCombine(words, 2));
        return sortIds;
	}

    private static String pickAndCombine(List<String> words, int pickChar){
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            word = word.toLowerCase();
            word = word.strip();
            if (word.isEmpty()) continue;

            if (word.length() > pickChar) {
                sb.append(word, 0, pickChar);
            }else {
                sb.append(word, 0, 1);
            }
        }
        return sb.toString();
    }

}
