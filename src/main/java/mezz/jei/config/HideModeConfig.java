package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HideModeConfig implements IHideModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final boolean defaultHideModeEnabled = false;
	private static final String[] defaultBlacklist = new String[]{};
	private final IModIdHelper modIdHelper;

	private boolean hideModeEnabled = defaultHideModeEnabled;
	private final Set<String> blacklist = new LinkedHashSet<>();

	@Nullable
	private final File blacklistConfigFile;

	public HideModeConfig(IModIdHelper modIdHelper, @Nullable File jeiConfigurationDir) {
		this.modIdHelper = modIdHelper;
		Collections.addAll(blacklist, defaultBlacklist);
		if (jeiConfigurationDir != null) {
			blacklistConfigFile = new File(jeiConfigurationDir, "blacklist.cfg");
			loadBlacklistConfig();
		} else {
			blacklistConfigFile = null;
		}
	}

	@Override
	public boolean isHideModeEnabled() {
		return hideModeEnabled;
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
	public <V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientRegistry ingredientRegistry, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		IIngredientType<V> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientListElement<V> element = IngredientListElementFactory.createUnorderedElement(ingredientRegistry, ingredientType, ingredient, modIdHelper);
		Preconditions.checkNotNull(element, "Failed to create element for blacklist");

		// combine item-level blacklist into wildcard-level ones
		if (blacklistType == IngredientBlacklistType.ITEM) {
			final String uid = getIngredientUid(ingredient, IngredientBlacklistType.ITEM, ingredientHelper);
			List<IIngredientListElement<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD));
			if (areAllBlacklisted(elementsToBeBlacklisted, uid)) {
				if (addIngredientToConfigBlacklist(ingredientFilter, element, ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper)) {
					saveBlacklist();
				}
				return;
			}
		}
		if (addIngredientToConfigBlacklist(ingredientFilter, element, ingredient, blacklistType, ingredientHelper)) {
			saveBlacklist();
		}
	}

	private <V> boolean addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientListElement<V> element, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		boolean updated = false;

		// remove lower-level blacklist entries when a higher-level one is added
		if (blacklistType == IngredientBlacklistType.WILDCARD) {
			List<IIngredientListElement<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, blacklistType));
			for (IIngredientListElement<V> elementToBeBlacklisted : elementsToBeBlacklisted) {
				String uid = getIngredientUid(elementToBeBlacklisted, IngredientBlacklistType.ITEM);
				updated |= blacklist.remove(uid);
			}
		}

		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		updated |= blacklist.add(uid);
		return updated;
	}

	private <V> boolean areAllBlacklisted(List<IIngredientListElement<V>> elements, String newUid) {
		for (IIngredientListElement<V> element : elements) {
			String uid = getIngredientUid(element, IngredientBlacklistType.ITEM);
			if (!uid.equals(newUid) && !blacklist.contains(uid)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <V> void removeIngredientFromConfigBlacklist(IngredientFilter ingredientFilter, IIngredientRegistry ingredientRegistry, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		IIngredientType<V> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientListElement<V> element = IngredientListElementFactory.createUnorderedElement(ingredientRegistry, ingredientType, ingredient, modIdHelper);
		Preconditions.checkNotNull(element, "Failed to create element for blacklist");

		boolean updated = false;

		if (blacklistType == IngredientBlacklistType.ITEM) {
			// deconstruct any wildcard blacklist since we are removing one element from it
			final String wildUid = getIngredientUid(ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper);
			if (blacklist.contains(wildUid)) {
				updated = true;
				blacklist.remove(wildUid);
				List<IIngredientListElement<V>> modMatches = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD));
				for (IIngredientListElement<V> modMatch : modMatches) {
					addIngredientToConfigBlacklist(ingredientFilter, modMatch, modMatch.getIngredient(), IngredientBlacklistType.ITEM, ingredientHelper);
				}
			}
		} else if (blacklistType == IngredientBlacklistType.WILDCARD) {
			// remove any item-level blacklist on items that match this wildcard
			List<IIngredientListElement<V>> modMatches = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD));
			for (IIngredientListElement<V> modMatch : modMatches) {
				final String uid = getIngredientUid(modMatch, IngredientBlacklistType.ITEM);
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

	private static <V> String getIngredientUid(@Nullable IIngredientListElement<V> element, IngredientBlacklistType blacklistType) {
		if (element == null) {
			return "";
		}
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
		return getIngredientUid(ingredient, blacklistType, ingredientHelper);
	}

	private static <V> String getIngredientUid(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		switch (blacklistType) {
			case ITEM:
				return ingredientHelper.getUniqueId(ingredient);
			case WILDCARD:
				return ingredientHelper.getWildcardId(ingredient);
			default:
				throw new IllegalStateException("Unknown blacklist type: " + blacklistType);
		}
	}
}
