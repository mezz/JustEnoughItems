package mezz.jei.config;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IngredientFilter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EditModeConfig implements IEditModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String[] defaultBlacklist = new String[]{};

	private final Set<String> blacklist = new LinkedHashSet<>();

	@Nullable
	private final File blacklistConfigFile;

	public EditModeConfig(@Nullable File jeiConfigurationDir) {
		Collections.addAll(blacklist, defaultBlacklist);
		if (jeiConfigurationDir != null) {
			blacklistConfigFile = new File(jeiConfigurationDir, "blacklist.cfg");
			loadBlacklistConfig();
		} else {
			blacklistConfigFile = null;
		}
	}

	private void loadBlacklistConfig() {
		if (blacklistConfigFile != null && blacklistConfigFile.exists()) {
			try (FileReader reader = new FileReader(blacklistConfigFile)) {
				List<String> strings = IOUtils.readLines(reader);
				blacklist.clear();
				blacklist.addAll(strings);
			} catch (IOException e) {
				LOGGER.error("Failed to load blacklist from file {}", blacklistConfigFile, e);
			}
		}
	}

	private void saveBlacklist() {
		if (blacklistConfigFile != null) {
			try {
				if (blacklistConfigFile.createNewFile()) {
					LOGGER.debug("Created blacklist config file: {}", blacklistConfigFile);
				}
				try (FileWriter writer = new FileWriter(blacklistConfigFile)) {
					IOUtils.writeLines(blacklist, "\n", writer);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to save blacklist to file {}", blacklistConfigFile, e);
			}
		}
	}

	@Override
	public <V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientManager ingredientManager, ITypedIngredient<V> typedIngredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		if (addIngredientToConfigBlacklist(ingredientFilter, typedIngredient, blacklistType, ingredientHelper)) {
			saveBlacklist();
		}
	}

	private <V> boolean addIngredientToConfigBlacklist(
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

		List<IIngredientListElementInfo<V>> wildcardMatches = ingredientFilter.searchForWildcardMatches(typedIngredient, ingredientHelper, wildcardUidFunction);
		return wildcardMatches.stream()
			.map(IIngredientListElementInfo::getTypedIngredient)
			.map(itemUidFunction)
			.collect(Collectors.toSet());
	}

	@Override
	public <V> void removeIngredientFromConfigBlacklist(
		IngredientFilter ingredientFilter,
		IIngredientManager ingredientManager,
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
			saveBlacklist();
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
}
