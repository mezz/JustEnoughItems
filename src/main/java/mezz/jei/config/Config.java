package mezz.jei.config;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraft.network.NetworkManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.GiveMode;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Config {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final String configKeyPrefix = "config.jei";

	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	public static final String defaultModNameFormatFriendly = ModIdFormattingConfig.defaultModNameFormatFriendly;
	public static final int smallestNumColumns = 2;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;
	public static final int maxRecipeGuiHeight = 5000;

	@Nullable
	private static LocalizedConfiguration config;
	private static final WorldConfig worldConfigState = new WorldConfig();
	@Nullable
	private static LocalizedConfiguration itemBlacklistConfig;
	@Nullable
	private static LocalizedConfiguration searchColorsConfig;
	@Nullable
	private static File bookmarkFile;

	private static final ConfigValues defaultValues = new ConfigValues();
	private static final ConfigValues values = new ConfigValues();
	private static final ModIdFormattingConfig modIdFormattingConfig = new ModIdFormattingConfig();

	// item blacklist
	private static final Set<String> itemBlacklist = new HashSet<>();
	private static final String[] defaultItemBlacklist = new String[]{};

	private static final IngredientFilterConfig ingredientFilterConfig = new IngredientFilterConfig(new IngredientFilterConfigValues());
	private static final HideModeConfig hideModeConfig = new HideModeConfig(worldConfigState::isEditModeEnabled, itemBlacklist);

	private Config() {

	}

	public static IIngredientFilterConfig getIngredientFilterConfig() {
		return ingredientFilterConfig;
	}

	public static IHideModeConfig getHideModeConfig() {
		return hideModeConfig;
	}

	public static IFilterTextSource getFilterTextSource() {
		return worldConfigState;
	}

	public static boolean isOverlayEnabled() {
		return worldConfigState.isOverlayEnabled();
	}

	public static void toggleOverlayEnabled() {
		worldConfigState.toggleOverlayEnabled();
	}

	public static boolean isBookmarkOverlayEnabled() {
		return worldConfigState.isBookmarkOverlayEnabled();
	}

	public static void toggleBookmarkEnabled() {
		worldConfigState.toggleBookmarkEnabled();
	}

	public static boolean isCheatItemsEnabled() {
		return worldConfigState.isCheatItemsEnabled();
	}

	public static void toggleCheatItemsEnabled() {
		worldConfigState.toggleCheatItemsEnabled();
	}

	public static void setCheatItemsEnabled(boolean value) {
		worldConfigState.setCheatItemsEnabled(value);
	}

	public static boolean isEditModeEnabled() {
		return worldConfigState.isEditModeEnabled();
	}

	public static void toggleEditModeEnabled() {
		worldConfigState.toggleEditModeEnabled();
	}

	public static boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return worldConfigState.isDeleteItemsInCheatModeActive();
	}

	public static boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	public static boolean isOptimizeMemoryUsage() {
		return values.optimizeMemoryUsage;
	}

	public static boolean isAddingBookmarksToFront() {
		return values.addBookmarksToFront;
	}

	public static GiveMode getGiveMode() {
		return values.giveMode;
	}

	public static String getModNameFormat() {
		return modIdFormattingConfig.getModNameFormat();
	}

	public static boolean isModNameFormatOverrideActive() {
		return modIdFormattingConfig.isModNameFormatOverrideActive();
	}

	public static void checkForModNameFormatOverride() {
		modIdFormattingConfig.checkForModNameFormatOverride();
	}

	public static boolean isHideIngredientsWhenNotSearchingEnabled() {
		return values.hideIngredientsWhenNotSearching;
	}

	public static void toggleHideIngredientsWhenNotSearching() {
		values.hideIngredientsWhenNotSearching = !values.hideIngredientsWhenNotSearching;
		if (config != null) {
			config.get(CATEGORY_ADVANCED, "hideIngredientsWhenNotSearching", defaultValues.hideIngredientsWhenNotSearching)
				.set(values.hideIngredientsWhenNotSearching);
			config.save();
		}
	}

	public static int getMaxColumns() {
		return values.maxColumns;
	}

	public static int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight;
	}

	public static SearchMode getModNameSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getModNameSearchMode().name());
	}

	public static SearchMode getTooltipSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getTooltipSearchMode().name());
	}

	public static SearchMode getOreDictSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getOreDictSearchMode().name());
	}

	public static SearchMode getCreativeTabSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getCreativeTabSearchMode().name());
	}

	public static SearchMode getColorSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getColorSearchMode().name());
	}

	public static SearchMode getResourceIdSearchMode() {
		return SearchMode.valueOf(ingredientFilterConfig.getResourceIdSearchMode().name());
	}

	public static boolean getSearchAdvancedTooltips() {
		return ingredientFilterConfig.getSearchAdvancedTooltips();
	}

	public enum SearchMode {
		ENABLED, REQUIRE_PREFIX, DISABLED
	}

	public static boolean setFilterText(String filterText) {
		return worldConfigState.setFilterText(filterText);
	}

	public static String getFilterText() {
		return worldConfigState.getFilterText();
	}

	public static void saveFilterText() {
		worldConfigState.saveFilterText();
	}

	@Nullable
	public static LocalizedConfiguration getConfig() {
		return config;
	}

	@Nullable
	public static Configuration getWorldConfig() {
		return worldConfigState.getConfig();
	}

	@Nullable
	public static File getBookmarkFile() {
		return bookmarkFile;
	}

	public static void preInit(FMLPreInitializationEvent event) {

		File jeiConfigurationDir = new File(event.getModConfigurationDirectory(), Constants.MOD_ID);
		if (!jeiConfigurationDir.exists()) {
			try {
				if (!jeiConfigurationDir.mkdir()) {
					LOGGER.error("Could not create config directory {}", jeiConfigurationDir);
					return;
				}
			} catch (SecurityException e) {
				LOGGER.error("Could not create config directory {}", jeiConfigurationDir, e);
				return;
			}
		}

		File minecraftDir = new File(Loader.instance().getConfigDir().getParent());
		bookmarkFile = new File(minecraftDir, "jei_bookmarks.ini");
		File oldBookmarkFile = new File(jeiConfigurationDir, "bookmarks.ini");
		if (!bookmarkFile.exists() && oldBookmarkFile.exists()) {
			try {
				if (!oldBookmarkFile.renameTo(bookmarkFile)) {
					LOGGER.error("Could not move the old bookmark file from {} to {}", jeiConfigurationDir, "./");
					return;
				}
			} catch (SecurityException e) {
				LOGGER.error("Could not move the old bookmark file from {} to {}", jeiConfigurationDir, "./", e);
				return;
			}
		}

		final File configFile = new File(jeiConfigurationDir, "jei.cfg");
		final File itemBlacklistConfigFile = new File(jeiConfigurationDir, "itemBlacklist.cfg");
		final File searchColorsConfigFile = new File(jeiConfigurationDir, "searchColors.cfg");
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		worldConfigState.setConfiguration(new Configuration(worldConfigFile, "0.1.0"));
		config = new LocalizedConfiguration(configKeyPrefix, configFile, "0.4.0");
		itemBlacklistConfig = new LocalizedConfiguration(configKeyPrefix, itemBlacklistConfigFile, "0.1.0");
		searchColorsConfig = new LocalizedConfiguration(configKeyPrefix, searchColorsConfigFile, "0.1.0");

		syncConfig();
		syncItemBlacklistConfig();
		syncSearchColorsConfig();
	}

	public static boolean syncAllConfig() {
		boolean needsReload = false;
		if (syncConfig()) {
			needsReload = true;
		}

		if (syncItemBlacklistConfig()) {
			needsReload = true;
		}

		NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
		if (syncWorldConfig(networkManager)) {
			needsReload = true;
		}

		if (syncSearchColorsConfig()) {
			needsReload = true;
		}

		return needsReload;
	}

	private static boolean syncConfig() {
		if (config == null) {
			return false;
		}
		boolean needsReload = false;

		config.addCategory(CATEGORY_ADVANCED);

		ConfigCategory modeCategory = config.getCategory("mode");
		if (modeCategory != null) {
			config.removeCategory(modeCategory);
		}

		ConfigCategory addonsCategory = config.getCategory("addons");
		if (addonsCategory != null) {
			config.removeCategory(addonsCategory);
		}

		ConfigCategory interfaceCategory = config.getCategory("interface");
		if (interfaceCategory != null) {
			config.removeCategory(interfaceCategory);
		}

		needsReload |= ingredientFilterConfig.syncConfig(config);

		ConfigCategory categoryAdvanced = config.getCategory(CATEGORY_ADVANCED);
		categoryAdvanced.remove("nbtKeyIgnoreList");
		categoryAdvanced.remove("deleteItemsInCheatModeEnabled");
		categoryAdvanced.remove("hideLaggyModelsEnabled");
		categoryAdvanced.remove("hideMissingModelsEnabled");
		categoryAdvanced.remove("debugItemEnabled");
		categoryAdvanced.remove("colorSearchEnabled");
		categoryAdvanced.remove("maxSubtypes");

		values.centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", defaultValues.centerSearchBarEnabled);
		values.hideIngredientsWhenNotSearching = config.getBoolean(CATEGORY_ADVANCED, "hideIngredientsWhenNotSearching", defaultValues.hideIngredientsWhenNotSearching);

		values.optimizeMemoryUsage = config.getBoolean(CATEGORY_ADVANCED, "optimizeMemoryUsage", defaultValues.optimizeMemoryUsage);

		values.addBookmarksToFront = config.getBoolean(CATEGORY_ADVANCED, "addBookmarksToFront", defaultValues.addBookmarksToFront);

		values.giveMode = config.getEnum("giveMode", CATEGORY_ADVANCED, defaultValues.giveMode, GiveMode.values());

		values.maxColumns = config.getInt("maxColumns", CATEGORY_ADVANCED, defaultValues.maxColumns, smallestNumColumns, largestNumColumns);

		values.maxRecipeGuiHeight = config.getInt("maxRecipeGuiHeight", CATEGORY_ADVANCED, defaultValues.maxRecipeGuiHeight, minRecipeGuiHeight, maxRecipeGuiHeight);

		modIdFormattingConfig.sync(config);

		{
			Property property = config.get(CATEGORY_ADVANCED, "debugModeEnabled", defaultValues.debugModeEnabled);
			property.setShowInGui(false);
			values.debugModeEnabled = property.getBoolean();
		}

		final boolean configChanged = config.hasChanged();
		if (configChanged) {
			config.save();
		}
		return needsReload;
	}

	static void migrateSearchAdvancedTooltipsConfig(Configuration config) {
		String legacyCategoryName = "searchAdvancedTooltips";
		String legacyPropertyName = CATEGORY_SEARCH;
		String propertyName = "searchAdvancedTooltips";
		if (!config.hasCategory(legacyCategoryName)) {
			return;
		}

		ConfigCategory legacyCategory = config.getCategory(legacyCategoryName);
		if (legacyCategory.containsKey(legacyPropertyName)) {
			if (config.hasKey(CATEGORY_SEARCH, propertyName)) {
				legacyCategory.remove(legacyPropertyName);
			} else if (config.moveProperty(legacyCategoryName, legacyPropertyName, CATEGORY_SEARCH)) {
				config.renameProperty(CATEGORY_SEARCH, legacyPropertyName, propertyName);
			}
		}
		if (legacyCategory.isEmpty()) {
			config.removeCategory(legacyCategory);
		}
	}

	public static String parseFriendlyModNameFormat(String formatWithEnumNames) {
		return ModIdFormattingConfig.parseFriendlyModNameFormat(formatWithEnumNames);
	}

	private static boolean syncItemBlacklistConfig() {
		if (itemBlacklistConfig == null) {
			return false;
		}
		itemBlacklistConfig.addCategory(CATEGORY_ADVANCED);

		String[] itemBlacklistArray = itemBlacklistConfig.getStringList("itemBlacklist", CATEGORY_ADVANCED, defaultItemBlacklist);
		itemBlacklist.clear();
		Collections.addAll(itemBlacklist, itemBlacklistArray);

		final boolean configChanged = itemBlacklistConfig.hasChanged();
		if (configChanged) {
			itemBlacklistConfig.save();
		}
		return configChanged;
	}

	public static boolean syncWorldConfig(@Nullable NetworkManager networkManager) {
		return worldConfigState.syncWorldConfig(networkManager);
	}

	private static boolean syncSearchColorsConfig() {
		if (searchColorsConfig == null) {
			return false;
		}
		searchColorsConfig.addCategory(CATEGORY_SEARCH_COLORS);

		final String[] searchColorDefaults = ColorGetter.getColorDefaults();
		final String[] searchColors = searchColorsConfig.getStringList("searchColors", CATEGORY_SEARCH_COLORS, searchColorDefaults);

		final ImmutableMap.Builder<Color, String> searchColorsMapBuilder = ImmutableMap.builder();
		for (String entry : searchColors) {
			final String[] values = entry.split(":");
			if (values.length != 2) {
				LOGGER.error("Invalid format for searchColor entry: {}", entry);
			} else {
				try {
					final String name = values[0];
					final int colorValue = Integer.decode("0x" + values[1]);
					final Color color = new Color(colorValue);
					searchColorsMapBuilder.put(color, name);
				} catch (NumberFormatException e) {
					LOGGER.error("Invalid number format for searchColor entry: {}", entry, e);
				}
			}
		}
		final ColorNamer colorNamer = new ColorNamer(searchColorsMapBuilder.build());
		Internal.setColorNamer(colorNamer);

		final boolean configChanged = searchColorsConfig.hasChanged();
		if (configChanged) {
			searchColorsConfig.save();
		}
		return configChanged;
	}

	private static void updateBlacklist() {
		if (itemBlacklistConfig == null) {
			return;
		}
		Property property = itemBlacklistConfig.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);

		String[] currentBlacklist = itemBlacklist.toArray(new String[0]);
		property.set(currentBlacklist);

		boolean changed = itemBlacklistConfig.hasChanged();
		if (changed) {
			itemBlacklistConfig.save();
		}
	}

	public static <V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientRegistry ingredientRegistry, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		IIngredientType<V> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientListElement<V> element = IngredientListElementFactory.createUnorderedElement(ingredientRegistry, ingredientType, ingredient, ForgeModIdHelper.getInstance());
		Preconditions.checkNotNull(element, "Failed to create element for blacklist");

		// combine item-level blacklist into wildcard-level ones
		if (blacklistType == IngredientBlacklistType.ITEM) {
			final String uid = getIngredientUid(ingredient, IngredientBlacklistType.ITEM, ingredientHelper);
			List<IIngredientListElement<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, IngredientBlacklistType.WILDCARD));
			if (areAllBlacklisted(elementsToBeBlacklisted, uid, IngredientBlacklistType.ITEM)) {
				if (addIngredientToConfigBlacklist(ingredientFilter, element, ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper)) {
					updateBlacklist();
				}
				return;
			}
		}
		if (addIngredientToConfigBlacklist(ingredientFilter, element, ingredient, blacklistType, ingredientHelper)) {
			updateBlacklist();
		}
	}

	private static <V> boolean addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientListElement<V> element, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		boolean updated = false;

		// remove lower-level blacklist entries when a higher-level one is added
		if (blacklistType == IngredientBlacklistType.WILDCARD) {
			List<IIngredientListElement<V>> elementsToBeBlacklisted = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, blacklistType));
			for (IIngredientListElement<V> elementToBeBlacklisted : elementsToBeBlacklisted) {
				String uid = getIngredientUid(elementToBeBlacklisted, IngredientBlacklistType.ITEM);
				updated |= itemBlacklist.remove(uid);
			}
		}

		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		updated |= itemBlacklist.add(uid);
		return updated;
	}

	private static <V> boolean areAllBlacklisted(List<IIngredientListElement<V>> elements, String newUid, IngredientBlacklistType blacklistType) {
		for (IIngredientListElement<V> element : elements) {
			String uid = getIngredientUid(element, blacklistType);
			if (!uid.equals(newUid) && !itemBlacklist.contains(uid)) {
				return false;
			}
		}
		return true;
	}

	public static <V> void removeIngredientFromConfigBlacklist(IngredientFilter ingredientFilter, IIngredientRegistry ingredientRegistry, V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		IIngredientType<V> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientListElement<V> element = IngredientListElementFactory.createUnorderedElement(ingredientRegistry, ingredientType, ingredient, ForgeModIdHelper.getInstance());
		Preconditions.checkNotNull(element, "Failed to create element for blacklist");

		boolean updated = false;

		// deconstruct any mod-id blacklists into lower-level ones first. mod-id blacklist is deprecated
		{
			final String modUid = getIngredientUid(ingredient, IngredientBlacklistType.MOD_ID, ingredientHelper);
			if (itemBlacklist.contains(modUid)) {
				updated = true;
				itemBlacklist.remove(modUid);
				List<IIngredientListElement<V>> modMatches = ingredientFilter.getMatches(element, (input) -> getIngredientUid(input, IngredientBlacklistType.MOD_ID));
				for (IIngredientListElement<V> modMatch : modMatches) {
					addIngredientToConfigBlacklist(ingredientFilter, modMatch, modMatch.getIngredient(), IngredientBlacklistType.ITEM, ingredientHelper);
				}
			}
		}

		if (blacklistType == IngredientBlacklistType.ITEM) {
			// deconstruct any wildcard blacklist since we are removing one element from it
			final String wildUid = getIngredientUid(ingredient, IngredientBlacklistType.WILDCARD, ingredientHelper);
			if (itemBlacklist.contains(wildUid)) {
				updated = true;
				itemBlacklist.remove(wildUid);
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
				updated |= itemBlacklist.remove(uid);
			}
		}

		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		updated |= itemBlacklist.remove(uid);
		if (updated) {
			updateBlacklist();
		}
	}

	public static <V> boolean isIngredientOnConfigBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		for (IngredientBlacklistType ingredientBlacklistType : IngredientBlacklistType.VALUES) {
			if (isIngredientOnConfigBlacklist(ingredient, ingredientBlacklistType, ingredientHelper)) {
				return true;
			}
		}
		return false;
	}

	public static <V> boolean isIngredientOnConfigBlacklist(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		return itemBlacklist.contains(uid);
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
			case MOD_ID:
				return ingredientHelper.getModId(ingredient);
			default:
				throw new IllegalStateException("Unknown blacklist type: " + blacklistType);
		}
	}

	/**
	 * https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
	 * Compares two version strings.
	 *
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 *
	 * note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 *
	 * @param str1 a string of ordinal numbers separated by decimal points.
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2.
	 * The result is a positive integer if str1 is _numerically_ greater than str2.
	 * The result is zero if the strings are _numerically_ equal.
	 */
	static int versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		return Integer.signum(vals1.length - vals2.length);
	}
}
