package mezz.jei.config;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.ingredients.IngredientListElementComparator;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


public final class Config {
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
	private static File jeiConfigurationDir;

	private static final ConfigValues defaultValues = new ConfigValues();
	private static final ConfigValues values = new ConfigValues();

	// item blacklist
	private static final Set<String> itemBlacklist = new HashSet<String>();
	private static final String[] defaultItemBlacklist = new String[]{};

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return values.overlayEnabled;
	}

	public static void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		if (worldConfig != null) {
			final String worldCategory = SessionData.getWorldUid();
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new OverlayToggleEvent(values.overlayEnabled));
	}

	public static boolean isCheatItemsEnabled() {
		return values.cheatItemsEnabled;
	}

	public static void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!values.cheatItemsEnabled);
	}

	public static void setCheatItemsEnabled(boolean value) {
		if (values.cheatItemsEnabled != value) {
			values.cheatItemsEnabled = value;

			if (worldConfig != null) {
				final String worldCategory = SessionData.getWorldUid();
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
				}
			}

			if (values.cheatItemsEnabled && SessionData.isJeiOnServer()) {
				JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	public static boolean isEditModeEnabled() {
		return values.editModeEnabled;
	}

	public static boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return values.cheatItemsEnabled && SessionData.isJeiOnServer();
	}

	public static boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	public static String getModNameFormat() {
		return values.modNameFormat;
	}

	public static int getMaxSubtypes() {
		return values.maxSubtypes;
	}

	public static SearchMode getModNameSearchMode() {
		return values.modNameSearchMode;
	}

	public static SearchMode getTooltipSearchMode() {
		return values.tooltipSearchMode;
	}

	public static SearchMode getOreDictSearchMode() {
		return values.oreDictSearchMode;
	}

	public static SearchMode getCreativeTabSearchMode() {
		return values.creativeTabSearchMode;
	}

	public static SearchMode getColorSearchMode() {
		return values.colorSearchMode;
	}

	public static SearchMode getResourceIdSearchMode() {
		return values.resourceIdSearchMode;
	}

	public static boolean getSearchAdvancedTooltips() {
		return values.searchAdvancedTooltips;
	}

	public enum SearchMode {
		ENABLED, REQUIRE_PREFIX, DISABLED
	}

	public static boolean setFilterText(String filterText) {
		if (values.filterText.equals(filterText)) {
			return false;
		} else {
			values.filterText = filterText;
			return true;
		}
	}

	public static String getFilterText() {
		return values.filterText;
	}

	public static void saveFilterText() {
		if (worldConfig != null) {
			final String worldCategory = SessionData.getWorldUid();
			Property property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
			property.set(values.filterText);

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
	
	public static String getSortOrder()
	{
		return values.itemSortlist;
	}

	public static File getJeiConfigurationDir() {
		Preconditions.checkState(jeiConfigurationDir != null);
		return jeiConfigurationDir;
	}

	public static void preInit(FMLPreInitializationEvent event) {

		jeiConfigurationDir = new File(event.getModConfigurationDirectory(), Constants.MOD_ID);
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
		worldConfig = new Configuration(worldConfigFile, "0.1.0");
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

		String loadedConfigVersion = config.getLoadedConfigVersion();
		// set new defaults moving to config version 0.3.0
		if (loadedConfigVersion != null && versionCompare(loadedConfigVersion, "0.3.0") < 0) {
			config.setEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultValues.creativeTabSearchMode, searchModes);
			config.setEnum("oreDictSearchMode", CATEGORY_SEARCH, defaultValues.oreDictSearchMode, searchModes);
		}

		// set new defaults moving to config version 0.4.0
		if (loadedConfigVersion != null && versionCompare(loadedConfigVersion, "0.4.0") < 0) {
			config.setInt("maxSubtypes", CATEGORY_ADVANCED, defaultValues.maxSubtypes, 10, 10000);
		}

		values.modNameSearchMode = config.getEnum("modNameSearchMode", CATEGORY_SEARCH, defaultValues.modNameSearchMode, searchModes);
		values.tooltipSearchMode = config.getEnum("tooltipSearchMode", CATEGORY_SEARCH, defaultValues.tooltipSearchMode, searchModes);
		values.oreDictSearchMode = config.getEnum("oreDictSearchMode", CATEGORY_SEARCH, defaultValues.oreDictSearchMode, searchModes);
		values.creativeTabSearchMode = config.getEnum("creativeTabSearchMode", CATEGORY_SEARCH, defaultValues.creativeTabSearchMode, searchModes);
		values.colorSearchMode = config.getEnum("colorSearchMode", CATEGORY_SEARCH, defaultValues.colorSearchMode, searchModes);
		values.resourceIdSearchMode = config.getEnum("resourceIdSearchMode", CATEGORY_SEARCH, defaultValues.resourceIdSearchMode, searchModes);
		if (config.getCategory(CATEGORY_SEARCH).hasChanged()) {
			needsReload = true;
		}

		values.searchAdvancedTooltips = config.getBoolean("searchAdvancedTooltips", CATEGORY_SEARCH, defaultValues.searchAdvancedTooltips);

		ConfigCategory categoryAdvanced = config.getCategory(CATEGORY_ADVANCED);
		categoryAdvanced.remove("nbtKeyIgnoreList");
		categoryAdvanced.remove("deleteItemsInCheatModeEnabled");
		categoryAdvanced.remove("hideLaggyModelsEnabled");
		categoryAdvanced.remove("hideMissingModelsEnabled");
		categoryAdvanced.remove("debugItemEnabled");
		categoryAdvanced.remove("colorSearchEnabled");

		values.centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", defaultValues.centerSearchBarEnabled);

		EnumSet<TextFormatting> validFormatting = EnumSet.allOf(TextFormatting.class);
		validFormatting.remove(TextFormatting.RESET);
		String[] validValues = new String[validFormatting.size()];
		int i = 0;
		for (TextFormatting formatting : validFormatting) {
			validValues[i] = formatting.getFriendlyName().toLowerCase(Locale.ENGLISH);
			i++;
		}
		String modNameFormatFriendly = config.getString("modNameFormat", CATEGORY_ADVANCED, defaultValues.modNameFormatFriendly, validValues);
		values.modNameFormat = parseFriendlyModNameFormat(modNameFormatFriendly);

		{
			String comment = Translator.translateToLocal("config.jei.advanced.maxSubtypes.comment");
			Property property = config.get(CATEGORY_ADVANCED, "maxSubtypes", defaultValues.maxSubtypes, comment, 10, 10000);
			property.setShowInGui(false);
			values.maxSubtypes = property.getInt();
		}

		{
			Property property = config.get(CATEGORY_ADVANCED, "debugModeEnabled", defaultValues.debugModeEnabled);
			property.setShowInGui(false);
			values.debugModeEnabled = property.getBoolean();
		}
		
		{

			//This also loads up the comparators.
			defaultValues.itemSortlist = IngredientListElementComparator.initConfig();
			Property property = config.get(CATEGORY_ADVANCED, "itemSortList", defaultValues.itemSortlist);			
			if (IngredientListElementComparator.INSTANCE != null) {
				IngredientListElementComparator.loadConfig(property.getString());
			}
			
			if (categoryAdvanced.get("itemSortList").hasChanged()) {
				needsReload = true;
			}
		}
				

		final boolean configChanged = config.hasChanged();
		if (configChanged) {
			config.save();
		}
		return needsReload;
	}

	public static String parseFriendlyModNameFormat(String formatWithEnumNames) {
		if (formatWithEnumNames.isEmpty()) {
			return "";
		}
		StringBuilder format = new StringBuilder();
		String[] strings = formatWithEnumNames.split(" ");
		for (String string : strings) {
			TextFormatting valueByName = TextFormatting.getValueByName(string);
			if (valueByName != null) {
				format.append(valueByName.toString());
			} else {
				Log.error("Invalid format: {}", string);
			}
		}
		return format.toString();
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

	public static boolean syncWorldConfig() {
		if (worldConfig == null) {
			return false;
		}

		final String worldCategory = SessionData.getWorldUid();

		Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
		property.setLanguageKey("config.jei.interface.overlayEnabled");
		property.setComment(Translator.translateToLocal("config.jei.interface.overlayEnabled.comment"));
		property.setShowInGui(false);
		values.overlayEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
		property.setLanguageKey("config.jei.mode.cheatItemsEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.cheatItemsEnabled.comment"));
		values.cheatItemsEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "editEnabled", defaultValues.editModeEnabled);
		property.setLanguageKey("config.jei.mode.editEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.editEnabled.comment"));
		values.editModeEnabled = property.getBoolean();
		if (property.hasChanged()) {
			MinecraftForge.EVENT_BUS.post(new EditModeToggleEvent(values.editModeEnabled));
		}

		property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
		property.setShowInGui(false);
		values.filterText = property.getString();

		final boolean configChanged = worldConfig.hasChanged();
		if (configChanged) {
			worldConfig.save();
		}
		return false;
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
			default:
				throw new IllegalStateException("Unknown blacklist type: " + blacklistType);
		}
	}

	public enum IngredientBlacklistType {
		ITEM, WILDCARD, MOD_ID;

		public static final IngredientBlacklistType[] VALUES = values();
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
	 *         The result is a positive integer if str1 is _numerically_ greater than str2.
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	private static int versionCompare(String str1, String str2) {
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
