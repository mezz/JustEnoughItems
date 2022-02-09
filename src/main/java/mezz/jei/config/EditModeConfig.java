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

import mezz.jei.api.ingredients.IIngredientHelper;
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
	public <V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientManager ingredientManager, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		// combine item-level blacklist into wildcard-level ones
		if (blacklistType == IngredientBlacklistType.ITEM) {
			final String uid = getIngredientUid(ingredient, IngredientBlacklistType.ITEM, ingredientHelper);
			List<IIngredientListElementInfo<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(ingredient, ingredientHelper, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD, ingredientHelper));
			if (areAllBlacklisted(elementsToBeBlacklisted, ingredientHelper, uid)) {
				if (addIngredientToConfigBlacklist(ingredientFilter, ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper)) {
					saveBlacklist();
				}
				return;
			}
		}
		if (addIngredientToConfigBlacklist(ingredientFilter, ingredient, blacklistType, ingredientHelper)) {
			saveBlacklist();
		}
	}

	private <V> boolean addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		boolean updated = false;

		// remove lower-level blacklist entries when a higher-level one is added
		if (blacklistType == IngredientBlacklistType.WILDCARD) {
			List<IIngredientListElementInfo<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(ingredient, ingredientHelper, (input) -> getIngredientUid(input, blacklistType, ingredientHelper));
			for (IIngredientListElementInfo<V> elementToBeBlacklistedInfo : elementsToBeBlacklisted) {
				V ingredientToBeBlacklisted = elementToBeBlacklistedInfo.getIngredient();
				String uid = getIngredientUid(ingredientToBeBlacklisted, IngredientBlacklistType.ITEM, ingredientHelper);
				updated |= blacklist.remove(uid);
			}
		}

		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		updated |= blacklist.add(uid);
		return updated;
	}

	private <V> boolean areAllBlacklisted(List<IIngredientListElementInfo<V>> elementInfos, IIngredientHelper<V> ingredientHelper, String newUid) {
		for (IIngredientListElementInfo<V> elementInfo : elementInfos) {
			V ingredient = elementInfo.getIngredient();
			String uid = getIngredientUid(ingredient, IngredientBlacklistType.ITEM, ingredientHelper);
			if (!uid.equals(newUid) && !blacklist.contains(uid)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <V> void removeIngredientFromConfigBlacklist(IngredientFilter ingredientFilter, IIngredientManager ingredientManager, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		boolean updated = false;

		if (blacklistType == IngredientBlacklistType.ITEM) {
			// deconstruct any wildcard blacklist since we are removing one element from it
			final String wildUid = getIngredientUid(ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper);
			if (blacklist.contains(wildUid)) {
				updated = true;
				blacklist.remove(wildUid);
				List<IIngredientListElementInfo<V>> modMatches = ingredientFilter.getMatches(ingredient, ingredientHelper, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD, ingredientHelper));
				for (IIngredientListElementInfo<V> modMatch : modMatches) {
					addIngredientToConfigBlacklist(ingredientFilter, modMatch.getIngredient(), IngredientBlacklistType.ITEM, ingredientHelper);
				}
			}
		} else if (blacklistType == IngredientBlacklistType.WILDCARD) {
			// remove any item-level blacklist on items that match this wildcard
			List<IIngredientListElementInfo<V>> modMatches = ingredientFilter.getMatches(ingredient, ingredientHelper, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD, ingredientHelper));
			for (IIngredientListElementInfo<V> modMatch : modMatches) {
				V matchIngredient = modMatch.getIngredient();
				final String uid = getIngredientUid(matchIngredient, IngredientBlacklistType.ITEM, ingredientHelper);
				updated |= blacklist.remove(uid);
			}
		}

		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		updated |= blacklist.remove(uid);
		if (updated) {
			saveBlacklist();
		}
	}

	@Override
	public <V> boolean isIngredientOnConfigBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		for (IngredientBlacklistType ingredientBlacklistType : IngredientBlacklistType.VALUES) {
			if (isIngredientOnConfigBlacklist(ingredient, ingredientBlacklistType, ingredientHelper)) {
				return true;
			}
		}
		return false;
	}

	public <V> boolean isIngredientOnConfigBlacklist(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		return blacklist.contains(uid);
	}

	private static <V> String getIngredientUid(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		return switch (blacklistType) {
			case ITEM -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
			case WILDCARD -> ingredientHelper.getWildcardId(ingredient);
		};
	}
}
