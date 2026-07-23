package mezz.jei.gui.search;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.search.SearchMode;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ElementPrefixParser {
	private final Char2ObjectMap<PrefixInfo<IListElementInfo<?>, IListElement<?>>> map = new Char2ObjectOpenHashMap<>();
	private final PrefixInfo<IListElementInfo<?>, IListElement<?>> noPrefix;

	public ElementPrefixParser(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig config,
		IColorHelper colorHelper,
		ISearchStorageBuilderFactory searchStorageBuilderFactory
	) {
		ISearchStorageBuilderFactory limitedStringStorageBuilderFactory = createLimitedStringStorageBuilderFactory(searchStorageBuilderFactory);

		this.noPrefix = new PrefixInfo<>(
			'\0',
			() -> SearchMode.ENABLED,
			IListElementInfo::getNames,
			searchStorageBuilderFactory
		);

		addPrefix(new PrefixInfo<>(
			'@',
			config.modNameSearchMode()::getValue,
			info -> info.getModNames(config),
			limitedStringStorageBuilderFactory
		));
		addPrefix(new PrefixInfo<>(
			'#',
			config.tagSearchMode()::getValue,
			e -> e.getTagStrings(ingredientManager),
			limitedStringStorageBuilderFactory
		));
		addPrefix(new PrefixInfo<>(
			'$',
			config.tooltipSearchMode()::getValue,
			e -> e.getTooltipStrings(config, ingredientManager),
			searchStorageBuilderFactory
		));
		addPrefix(new PrefixInfo<>(
			'%',
			config.creativeTabSearchMode()::getValue,
			e -> e.getCreativeTabsStrings(ingredientManager),
			limitedStringStorageBuilderFactory
		));
		addPrefix(new PrefixInfo<>(
			'^',
			config.colorSearchMode()::getValue,
			e -> e.getColorNames(ingredientManager, colorHelper),
			limitedStringStorageBuilderFactory
		));
		addPrefix(new PrefixInfo<>(
			'&',
			config.resourceLocationSearchMode()::getValue,
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
		};
	}

	private void addPrefix(PrefixInfo<IListElementInfo<?>, IListElement<?>> info) {
		this.map.put(info.getPrefix(), info);
	}

	public Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> allPrefixInfos() {
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> values = new ArrayList<>(map.values());
		values.add(noPrefix);
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

}
