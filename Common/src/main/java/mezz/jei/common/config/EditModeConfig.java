package mezz.jei.common.config;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.core.config.IngredientBlacklistType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EditModeConfig implements IEditModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String[] defaultBlacklist = new String[]{};

	private final Set<String> blacklist = new LinkedHashSet<>();
	private final ISerializer serializer;

	public EditModeConfig(ISerializer serializer) {
		Collections.addAll(blacklist, defaultBlacklist);
		this.serializer = serializer;
		this.serializer.load(this);
	}

	@Override
	public <V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, ITypedIngredient<V> typedIngredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		if (addIngredientToConfigBlacklistInternal(ingredientFilter, typedIngredient, blacklistType, ingredientHelper)) {
			serializer.save(this);
		}
	}

	private <V> boolean addIngredientToConfigBlacklistInternal(
		IngredientFilter ingredientFilter,
		ITypedIngredient<V> typedIngredient,
		IngredientBlacklistType blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		String wildcardUid = getIngredientUid(typedIngredient, IngredientBlacklistType.WILDCARD, ingredientHelper);

		if (blacklistType == IngredientBlacklistType.ITEM) {
			String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);

			if (wildcardUid.equals(uid)) {
				// there's only one type of this ingredient, adding it as ITEM the same as adding it as WILDCARD.
				return blacklist.add(wildcardUid);
			}

			boolean updated = blacklist.add(uid);
			if (updated) {
				Set<String> wildcardIndividualUids = getMatchingItemUids(typedIngredient, ingredientHelper, ingredientFilter);
				if (blacklist.containsAll(wildcardIndividualUids)) {
					// Everything matching is blacklisted, upgrade this to WILDCARD
					blacklist.removeAll(wildcardIndividualUids);
					blacklist.add(wildcardUid);
				}
			}
			return updated;
		} else if (blacklistType == IngredientBlacklistType.WILDCARD) {
			Set<String> wildcardIndividualUids = getMatchingItemUids(typedIngredient, ingredientHelper, ingredientFilter);
			// remove lower-level blacklist entries when a higher-level one is added
			return blacklist.removeAll(wildcardIndividualUids) | blacklist.add(wildcardUid);
		}

		return false;
	}

	private static <V> Set<String> getMatchingItemUids(
		ITypedIngredient<V> typedIngredient,
		IIngredientHelper<V> ingredientHelper,
		IngredientFilter ingredientFilter
	) {
		Function<ITypedIngredient<V>, String> wildcardUidFunction = (i) -> getIngredientUid(i, IngredientBlacklistType.WILDCARD, ingredientHelper);
		Function<ITypedIngredient<V>, String> itemUidFunction = (i) -> getIngredientUid(i, IngredientBlacklistType.ITEM, ingredientHelper);

		{
			String itemUid = itemUidFunction.apply(typedIngredient);
			String wildcardUid = wildcardUidFunction.apply(typedIngredient);
			if (itemUid.equals(wildcardUid)) {
				return Set.of(itemUid);
			}
		}

		List<ITypedIngredient<V>> wildcardMatches = ingredientFilter.searchForWildcardMatches(typedIngredient, ingredientHelper, wildcardUidFunction);
		return wildcardMatches.stream()
			.map(itemUidFunction)
			.collect(Collectors.toSet());
	}

	@Override
	public <V> void removeIngredientFromConfigBlacklist(
		IngredientFilter ingredientFilter,
		ITypedIngredient<V> typedIngredient,
		IngredientBlacklistType blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		boolean updated = false;

		if (blacklistType == IngredientBlacklistType.ITEM) {
			// deconstruct any wildcard blacklist since we are removing one element from it later
			final String wildUid = getIngredientUid(typedIngredient, IngredientBlacklistType.WILDCARD, ingredientHelper);
			if (blacklist.contains(wildUid)) {
				blacklist.remove(wildUid);
				Set<String> uids = getMatchingItemUids(typedIngredient, ingredientHelper, ingredientFilter);
				blacklist.addAll(uids);
				updated = true;
			}
		} else if (blacklistType == IngredientBlacklistType.WILDCARD) {
			// remove any item-level blacklist on items that match this wildcard
			Set<String> uids = getMatchingItemUids(typedIngredient, ingredientHelper, ingredientFilter);
			updated = blacklist.removeAll(uids);
		}

		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		updated |= blacklist.remove(uid);
		if (updated) {
			serializer.save(this);
		}
	}

	@Override
	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		for (IngredientBlacklistType ingredientBlacklistType : IngredientBlacklistType.VALUES) {
			if (isIngredientOnConfigBlacklist(typedIngredient, ingredientBlacklistType, ingredientHelper)) {
				return true;
			}
		}
		return false;
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		return blacklist.contains(uid);
	}

	private static <V> String getIngredientUid(ITypedIngredient<V> typedIngredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final V ingredient = typedIngredient.getIngredient();
		return switch (blacklistType) {
			case ITEM -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
			case WILDCARD -> ingredientHelper.getWildcardId(ingredient);
		};
	}

	public interface ISerializer {
		void save(EditModeConfig config);
		void load(EditModeConfig config);
	}

	public static class FileSerializer implements ISerializer {
		private final Path path;

		public FileSerializer(Path path) {
			this.path = path;
		}

		@Override
		public void save(EditModeConfig config) {
			try {
				Files.write(path, config.blacklist);
				LOGGER.debug("Saved blacklist config to file: {}", path);
			} catch (IOException e) {
				LOGGER.error("Failed to save blacklist config to file {}", path, e);
			}
		}

		@Override
		public void load(EditModeConfig config) {
			try {
				List<String> strings = Files.readAllLines(path);
				config.blacklist.clear();
				config.blacklist.addAll(strings);
			} catch (IOException e) {
				LOGGER.error("Failed to load blacklist from file {}", path, e);
			}
		}
	}
}
