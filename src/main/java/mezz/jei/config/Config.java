package mezz.jei.config;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import mezz.jei.util.color.ColorGetter;
import mezz.jei.util.color.ColorNamer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	private static final String configKeyPrefix = "config.jei";

	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	@Nullable
	private static LocalizedConfiguration config;
	@Nullable
	private static Configuration worldConfig;
	@Nullable
	private static LocalizedConfiguration itemBlacklistConfig;
	@Nullable
	private static LocalizedConfiguration searchColorsConfig;
	@Nullable
	private static LocalizedConfiguration bookmarksConfig;

	// advanced
	private static boolean debugModeEnabled = false;
	private static boolean centerSearchBarEnabled = false;
	private static final String defaultModNameFormatFriendly = "blue italic";
	private static String modNameFormat = parseFriendlyModNameFormat(defaultModNameFormatFriendly);

	// search
	private static final SearchMode defaultModNameSearchMode = SearchMode.REQUIRE_PREFIX;
	private static final SearchMode defaultTooltipSearchMode = SearchMode.ENABLED;
	private static final SearchMode defaultOreDictSearchMode = SearchMode.REQUIRE_PREFIX;
	private static final SearchMode defaultCreativeTabSearchMode = SearchMode.REQUIRE_PREFIX;
	private static final SearchMode defaultColorSearchMode = SearchMode.DISABLED;

	private static SearchMode modNameSearchMode = defaultModNameSearchMode;
	private static SearchMode tooltipSearchMode = defaultTooltipSearchMode;
	private static SearchMode oreDictSearchMode = defaultOreDictSearchMode;
	private static SearchMode creativeTabSearchMode = defaultCreativeTabSearchMode;
	private static SearchMode colorSearchMode = defaultColorSearchMode;

	// per-world
	private static final boolean defaultOverlayEnabled = true;
	private static final boolean defaultCheatItemsEnabled = false;
	private static final boolean defaultEditModeEnabled = false;
	private static final String defaultFilterText = "";

	private static boolean overlayEnabled = defaultOverlayEnabled;
	private static boolean cheatItemsEnabled = defaultCheatItemsEnabled;
	private static boolean editModeEnabled = defaultEditModeEnabled;
	private static String filterText = defaultFilterText;

	// item blacklist
	private static final Set<String> itemBlacklist = new HashSet<String>();
	private static final String[] defaultItemBlacklist = new String[] {};

	// bookmarks
	private static final String[] defaultBookmarks = new String[] {};
	private static String[] bookmarks;

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return overlayEnabled;
	}

	public static void toggleOverlayEnabled() {
		overlayEnabled = !overlayEnabled;

		if (worldConfig != null) {
			final String worldCategory = SessionData.getWorldUid();
			Property property = worldConfig.get(worldCategory, "overlayEnabled", overlayEnabled);
			property.set(overlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new OverlayToggleEvent(overlayEnabled));
	}

	public static boolean isCheatItemsEnabled() {
		return cheatItemsEnabled;
	}

	public static void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!cheatItemsEnabled);
	}

	public static void setCheatItemsEnabled(boolean value) {
		if (cheatItemsEnabled != value) {
			cheatItemsEnabled = value;

			if (worldConfig != null) {
				final String worldCategory = SessionData.getWorldUid();
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", cheatItemsEnabled);
				property.set(cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
				}
			}

			if (cheatItemsEnabled && SessionData.isJeiOnServer()) {
				JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	public static boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	public static boolean isDebugModeEnabled() {
		return debugModeEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return cheatItemsEnabled && SessionData.isJeiOnServer();
	}

	public static boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled;
	}

	public static String getModNameFormat() {
		return modNameFormat;
	}

	public static SearchMode getModNameSearchMode() {
		return modNameSearchMode;
	}

	public static SearchMode getTooltipSearchMode() {
		return tooltipSearchMode;
	}

	public static SearchMode getOreDictSearchMode() {
		return oreDictSearchMode;
	}

	public static SearchMode getCreativeTabSearchMode() {
		return creativeTabSearchMode;
	}

	public static SearchMode getColorSearchMode() {
		return colorSearchMode;
	}

	public enum SearchMode {
		ENABLED, REQUIRE_PREFIX, DISABLED
	}

	public static boolean setFilterText(String filterText) {
		String lowercaseFilterText = filterText.toLowerCase();
		if (Config.filterText.equals(lowercaseFilterText)) {
			return false;
		}

		Config.filterText = lowercaseFilterText;
		return true;
	}

	public static String getFilterText() {
		return filterText;
	}

	public static void saveFilterText() {
		if (worldConfig != null) {
			final String worldCategory = SessionData.getWorldUid();
			Property property = worldConfig.get(worldCategory, "filterText", defaultFilterText);
			property.set(Config.filterText);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}
	}

	@Nullable
	public static LocalizedConfiguration getConfig() {
		return config;
	}

	@Nullable
	public static Configuration getWorldConfig() {
		return worldConfig;
	}

	public static void preInit(FMLPreInitializationEvent event) {

		File jeiConfigurationDir = new File(event.getModConfigurationDirectory(), Constants.MOD_ID);
		if (!jeiConfigurationDir.exists()) {
			try {
				if (!jeiConfigurationDir.mkdir()) {
					Log.error("Could not create config directory {}", jeiConfigurationDir);
					return;
				}
			} catch (SecurityException e) {
				Log.error("Could not create config directory {}", jeiConfigurationDir, e);
				return;
			}
		}

		final File configFile = new File(jeiConfigurationDir, "jei.cfg");
		final File itemBlacklistConfigFile = new File(jeiConfigurationDir, "itemBlacklist.cfg");
		final File searchColorsConfigFile = new File(jeiConfigurationDir, "searchColors.cfg");
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		final File bookmarksConfigFile = new File(jeiConfigurationDir, "bookmarks.cfg");

		worldConfig = new Configuration(worldConfigFile, "0.1.0");

		{
			final File oldConfigFile = event.getSuggestedConfigurationFile();
			if (oldConfigFile.exists()) {
				try {
					if (!oldConfigFile.renameTo(configFile)) {
						Log.error("Could not move old config file {}", oldConfigFile);
					}
				} catch (SecurityException e) {
					Log.error("Could not move old config file {}", oldConfigFile, e);
				}
			}
		}

		{
			final File oldItemBlacklistConfigFile = new File(event.getModConfigurationDirectory(), Constants.MOD_ID + "-itemBlacklist.cfg");
			if (oldItemBlacklistConfigFile.exists()) {
				try {
					if (!oldItemBlacklistConfigFile.renameTo(itemBlacklistConfigFile)) {
						Log.error("Could not move old config file {}", oldItemBlacklistConfigFile);
					}
				} catch (SecurityException e) {
					Log.error("Could not move old config file {}", oldItemBlacklistConfigFile, e);
				}
			}
		}

		config = new LocalizedConfiguration(configKeyPrefix, configFile, "0.2.0");
		itemBlacklistConfig = new LocalizedConfiguration(configKeyPrefix, itemBlacklistConfigFile, "0.1.0");
		searchColorsConfig = new LocalizedConfiguration(configKeyPrefix, searchColorsConfigFile, "0.1.0");
		bookmarksConfig = new LocalizedConfiguration(configKeyPrefix, bookmarksConfigFile, "0.1.0");

		syncConfig();
		syncItemBlacklistConfig();
		syncSearchColorsConfig();
		syncBookmarksConfig();
	}

	public static boolean syncAllConfig() {
		boolean needsReload = false;
		if (syncConfig()) {
			needsReload = true;
		}

		if (syncItemBlacklistConfig()) {
			needsReload = true;
		}

		if (syncWorldConfig()) {
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

		config.addCategory(CATEGORY_SEARCH);
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

		ConfigCategory searchCategory = config.getCategory(CATEGORY_SEARCH);
		searchCategory.remove("atPrefixRequiredForModName");
		searchCategory.remove("prefixRequiredForTooltipSearch");
		searchCategory.remove("prefixRequiredForOreDictSearch");
		searchCategory.remove("prefixRequiredForCreativeTabSearch");
		searchCategory.remove("prefixRequiredForColorSearch");

		SearchMode[] searchModes = SearchMode.values();
		modNameSearchMode = config.getEnum("modNameSearchMode", CATEGORY_SEARCH, defaultModNameSearchMode, searchModes);
		tooltipSearchMode = config.getEnum("tooltipSearchMode", CATEGORY_SEARCH, defaultTooltipSearchMode, searchModes);
		oreDictSearchMode = config.getEnum("oreDictSearchMode", CATEGORY_SEARCH, defaultOreDictSearchMode, searchModes);
		creativeTabSearchMode = config.getEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultCreativeTabSearchMode, searchModes);
		colorSearchMode = config.getEnum("colorSearchMode", CATEGORY_SEARCH, defaultColorSearchMode, searchModes);
		if (config.getCategory(CATEGORY_SEARCH).hasChanged()) {
			needsReload = true;
		}

		ConfigCategory categoryAdvanced = config.getCategory(CATEGORY_ADVANCED);
		categoryAdvanced.remove("nbtKeyIgnoreList");
		categoryAdvanced.remove("deleteItemsInCheatModeEnabled");
		categoryAdvanced.remove("hideLaggyModelsEnabled");
		categoryAdvanced.remove("hideMissingModelsEnabled");
		categoryAdvanced.remove("debugItemEnabled");
		categoryAdvanced.remove("colorSearchEnabled");

		centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", centerSearchBarEnabled);

		EnumSet<TextFormatting> validFormatting = EnumSet.allOf(TextFormatting.class);
		validFormatting.remove(TextFormatting.RESET);
		String[] validValues = new String[validFormatting.size()];
		int i = 0;
		for (TextFormatting formatting : validFormatting) {
			validValues[i] = formatting.getFriendlyName().toLowerCase(Locale.ENGLISH);
			i++;
		}
		String modNameFormatFriendly = config.getString("modNameFormat", CATEGORY_ADVANCED, defaultModNameFormatFriendly, validValues);
		modNameFormat = parseFriendlyModNameFormat(modNameFormatFriendly);

		debugModeEnabled = config.getBoolean(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
		{
			Property property = config.get(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
			property.setShowInGui(false);
		}

		// migrate item blacklist to new file
		if (itemBlacklistConfig != null && config.hasKey(CATEGORY_ADVANCED, "itemBlacklist")) {
			Property oldItemBlacklistProperty = config.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);
			String[] itemBlacklistArray = oldItemBlacklistProperty.getStringList();
			Property newItemBlacklistProperty = itemBlacklistConfig.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);
			newItemBlacklistProperty.set(itemBlacklistArray);
			categoryAdvanced.remove("itemBlacklist");
		}

		final boolean configChanged = config.hasChanged();
		if (configChanged) {
			config.save();
		}
		return needsReload;
	}

	private static String parseFriendlyModNameFormat(String formatWithEnumNames) {
		String format = "";
		if (formatWithEnumNames.isEmpty()) {
			return format;
		}
		String[] strings = formatWithEnumNames.split(" ");
		for (String string : strings) {
			TextFormatting valueByName = TextFormatting.getValueByName(string);
			if (valueByName != null) {
				format += valueByName.toString();
			} else {
				Log.error("Invalid format: {}", string);
			}
		}
		return format;
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

	private static boolean syncBookmarksConfig() {
		if (bookmarksConfig == null) {
			return false;
		}

		bookmarksConfig.addCategory(CATEGORY_ADVANCED);

		bookmarks = bookmarksConfig.getStringList("bookmarks", CATEGORY_ADVANCED, defaultBookmarks);

		final boolean configChanged = bookmarksConfig.hasChanged();
		if (configChanged) {
			bookmarksConfig.save();
		}
		return configChanged;
	}

	public static boolean updateBookmarks(String[] bookmarkIds) {
		bookmarks = bookmarkIds;
		if (bookmarksConfig == null) {
			return false;
		}
		Property property = bookmarksConfig.get(CATEGORY_ADVANCED, "bookmarks", defaultBookmarks);

		property.set(bookmarks);

		boolean changed = bookmarksConfig.hasChanged();
		if (changed) {
			bookmarksConfig.save();
		}
		return changed;
	}

	public static String[] getBookmarks() {
		return bookmarks;
	}

	public static boolean syncWorldConfig() {
		if (worldConfig == null) {
			return false;
		}

		boolean needsReload = false;
		final String worldCategory = SessionData.getWorldUid();

		Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultOverlayEnabled);
		property.setLanguageKey("config.jei.interface.overlayEnabled");
		property.setComment(Translator.translateToLocal("config.jei.interface.overlayEnabled.comment"));
		property.setShowInGui(false);
		overlayEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultCheatItemsEnabled);
		property.setLanguageKey("config.jei.mode.cheatItemsEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.cheatItemsEnabled.comment"));
		cheatItemsEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "editEnabled", defaultEditModeEnabled);
		property.setLanguageKey("config.jei.mode.editEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.editEnabled.comment"));
		editModeEnabled = property.getBoolean();
		if (property.hasChanged()) {
			needsReload = true;
		}

		property = worldConfig.get(worldCategory, "filterText", defaultFilterText);
		property.setShowInGui(false);
		filterText = property.getString();

		final boolean configChanged = worldConfig.hasChanged();
		if (configChanged) {
			worldConfig.save();
		}
		return needsReload;
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
				Log.error("Invalid format for searchColor entry: {}", entry);
			} else {
				try {
					final String name = values[0];
					final int colorValue = Integer.decode("0x" + values[1]);
					final Color color = new Color(colorValue);
					searchColorsMapBuilder.put(color, name);
				} catch (NumberFormatException e) {
					Log.error("Invalid number format for searchColor entry: {}", entry, e);
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

	private static boolean updateBlacklist() {
		if (itemBlacklistConfig == null) {
			return false;
		}
		Property property = itemBlacklistConfig.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);

		String[] currentBlacklist = itemBlacklist.toArray(new String[itemBlacklist.size()]);
		property.set(currentBlacklist);

		boolean changed = itemBlacklistConfig.hasChanged();
		if (changed) {
			itemBlacklistConfig.save();
		}
		return changed;
	}

	public static <V> void addIngredientToConfigBlacklist(V itemStack, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(itemStack, blacklistType, ingredientHelper);
		if (itemBlacklist.add(uid)) {
			updateBlacklist();
		}
	}

	public static <V> void removeIngredientFromConfigBlacklist(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(ingredient, blacklistType, ingredientHelper);
		if (itemBlacklist.remove(uid)) {
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

	private static <V> String getIngredientUid(V ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper) {
		switch (blacklistType) {
			case ITEM:
				return ingredientHelper.getUniqueId(ingredient);
			case WILDCARD:
				return ingredientHelper.getWildcardId(ingredient);
			case MOD_ID:
				return ingredientHelper.getModId(ingredient);
		}
		return "";
	}

	public enum IngredientBlacklistType {
		ITEM, WILDCARD, MOD_ID;

		public static final IngredientBlacklistType[] VALUES = values();
	}
}
