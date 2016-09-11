package mezz.jei.config;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.util.Log;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import mezz.jei.util.color.ColorGetter;
import mezz.jei.util.color.ColorNamer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	private static final String configKeyPrefix = "config.jei";
	private static File jeiConfigurationDir;

	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	private static LocalizedConfiguration config;
	private static Configuration worldConfig;
	private static LocalizedConfiguration itemBlacklistConfig;
	private static LocalizedConfiguration searchColorsConfig;

	// advanced
	private static boolean debugModeEnabled = false;
	private static boolean debugItemEnabled = false;
	private static boolean hideMissingModelsEnabled = true;
	private static boolean colorSearchEnabled = false;
	private static boolean centerSearchBarEnabled = false;

	// search
	private static boolean prefixRequiredForModNameSearch = true;
	private static boolean prefixRequiredForTooltipSearch = false;
	private static boolean prefixRequiredForOreDictSearch = true;
	private static boolean prefixRequiredForCreativeTabSearch = true;
	private static boolean prefixRequiredForColorSearch = true;

	// per-world
	private static final boolean defaultOverlayEnabled = true;
	private static final boolean defaultCheatItemsEnabled = false;
	private static final boolean defaultEditModeEnabled = false;
	private static final String defaultFilterText = "";

	private static boolean overlayEnabled = defaultOverlayEnabled;
	private static boolean cheatItemsEnabled = defaultCheatItemsEnabled;
	private static boolean editModeEnabled = defaultEditModeEnabled;
	@Nonnull
	private static String filterText = defaultFilterText;

	// item blacklist
	private static final Set<String> itemBlacklist = new HashSet<String>();
	private static final String[] defaultItemBlacklist = new String[]{};

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return overlayEnabled;
	}

	public static void toggleOverlayEnabled() {
		overlayEnabled = !overlayEnabled;

		final String worldCategory = SessionData.getWorldUid();
		Property property = worldConfig.get(worldCategory, "overlayEnabled", overlayEnabled);
		property.set(overlayEnabled);

		if (worldConfig.hasChanged()) {
			worldConfig.save();
		}
	}

	public static boolean isCheatItemsEnabled() {
		return cheatItemsEnabled;
	}

	public static void toggleCheatItemsEnabled() {
		cheatItemsEnabled = !cheatItemsEnabled;
	}

	public static boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	public static boolean isDebugModeEnabled() {
		return debugModeEnabled;
	}

	public static boolean isDebugItemEnabled() {
		return debugItemEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return cheatItemsEnabled && SessionData.isJeiOnServer();
	}

	public static boolean isHideMissingModelsEnabled() {
		return hideMissingModelsEnabled;
	}

	public static boolean isColorSearchEnabled() {
		return colorSearchEnabled;
	}

	public static boolean isCenterSearchBarEnabled() {
		return centerSearchBarEnabled;
	}

	public static boolean isPrefixRequiredForModNameSearch() {
		return prefixRequiredForModNameSearch;
	}

	public static boolean isPrefixRequiredForTooltipSearch() {
		return prefixRequiredForTooltipSearch;
	}

	public static boolean isPrefixRequiredForOreDictSearch() {
		return prefixRequiredForOreDictSearch;
	}

	public static boolean isPrefixRequiredForCreativeTabSearch() {
		return prefixRequiredForCreativeTabSearch;
	}

	public static boolean isPrefixRequiredForColorSearch() {
		return prefixRequiredForColorSearch;
	}

	public static boolean setFilterText(@Nonnull String filterText) {
		String lowercaseFilterText = filterText.toLowerCase();
		if (Config.filterText.equals(lowercaseFilterText)) {
			return false;
		}

		Config.filterText = lowercaseFilterText;
		return true;
	}

	@Nonnull
	public static String getFilterText() {
		return filterText;
	}

	public static void saveFilterText() {
		final String worldCategory = SessionData.getWorldUid();
		Property property = worldConfig.get(worldCategory, "filterText", defaultFilterText);
		property.set(Config.filterText);

		if (worldConfig.hasChanged()) {
			worldConfig.save();
		}
	}

	public static LocalizedConfiguration getConfig() {
		return config;
	}

	public static Configuration getWorldConfig() {
		return worldConfig;
	}

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {

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

		syncConfig();
		syncItemBlacklistConfig();
	}

	public static void startJei() {
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		worldConfig = new Configuration(worldConfigFile, "0.1.0");
		syncWorldConfig();
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

		prefixRequiredForModNameSearch = config.getBoolean(CATEGORY_SEARCH, "atPrefixRequiredForModName", prefixRequiredForModNameSearch);
		prefixRequiredForTooltipSearch = config.getBoolean(CATEGORY_SEARCH, "prefixRequiredForTooltipSearch", prefixRequiredForTooltipSearch);
		prefixRequiredForOreDictSearch = config.getBoolean(CATEGORY_SEARCH, "prefixRequiredForOreDictSearch", prefixRequiredForOreDictSearch);
		prefixRequiredForCreativeTabSearch = config.getBoolean(CATEGORY_SEARCH, "prefixRequiredForCreativeTabSearch", prefixRequiredForCreativeTabSearch);
		prefixRequiredForColorSearch = config.getBoolean(CATEGORY_SEARCH, "prefixRequiredForColorSearch", prefixRequiredForColorSearch);
		if (config.getCategory(CATEGORY_SEARCH).hasChanged()) {
			needsReload = true;
		}

		ConfigCategory categoryAdvanced = config.getCategory(CATEGORY_ADVANCED);
		categoryAdvanced.remove("nbtKeyIgnoreList");
		categoryAdvanced.remove("deleteItemsInCheatModeEnabled");
		categoryAdvanced.remove("hideLaggyModelsEnabled");

		hideMissingModelsEnabled = config.getBoolean(CATEGORY_ADVANCED, "hideMissingModelsEnabled", hideMissingModelsEnabled);
		if (categoryAdvanced.get("hideMissingModelsEnabled").hasChanged()) {
			needsReload = true;
		}

		colorSearchEnabled = config.getBoolean(CATEGORY_ADVANCED, "colorSearchEnabled", colorSearchEnabled);
		if (categoryAdvanced.get("colorSearchEnabled").hasChanged()) {
			needsReload = true;
		}

		centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", centerSearchBarEnabled);

		debugModeEnabled = config.getBoolean(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
		{
			Property property = config.get(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
			property.setShowInGui(false);
		}

		debugItemEnabled = config.getBoolean(CATEGORY_ADVANCED, "debugItemEnabled", debugItemEnabled);
		{
			Property property = config.get(CATEGORY_ADVANCED, "debugItemEnabled", debugItemEnabled);
			property.setShowInGui(false);
			property.setRequiresMcRestart(true);
		}

		// migrate item blacklist to new file
		if (config.hasKey(CATEGORY_ADVANCED, "itemBlacklist")) {
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

	private static boolean syncItemBlacklistConfig() {
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

	private static boolean syncWorldConfig() {
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
		Property property = itemBlacklistConfig.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);

		String[] currentBlacklist = itemBlacklist.toArray(new String[itemBlacklist.size()]);
		property.set(currentBlacklist);

		boolean changed = itemBlacklistConfig.hasChanged();
		if (changed) {
			itemBlacklistConfig.save();
		}
		return changed;
	}

	public static void addItemToConfigBlacklist(@Nonnull ItemStack itemStack, @Nonnull ItemBlacklistType blacklistType) {
		final String uid = getItemStackUid(itemStack, blacklistType);
		if (itemBlacklist.add(uid)) {
			updateBlacklist();
		}
	}

	public static void removeItemFromConfigBlacklist(@Nonnull ItemStack itemStack, @Nonnull ItemBlacklistType blacklistType) {
		final String uid = getItemStackUid(itemStack, blacklistType);
		if (itemBlacklist.remove(uid)) {
			updateBlacklist();
		}
	}

	public static boolean isItemOnConfigBlacklist(@Nonnull ItemStack itemStack) {
		for (ItemBlacklistType itemBlacklistType : ItemBlacklistType.VALUES) {
			if (isItemOnConfigBlacklist(itemStack, itemBlacklistType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isItemOnConfigBlacklist(@Nonnull ItemStack itemStack, @Nonnull ItemBlacklistType blacklistType) {
		final String uid = getItemStackUid(itemStack, blacklistType);
		return itemBlacklist.contains(uid);
	}

	private static String getItemStackUid(@Nonnull ItemStack itemStack, @Nonnull ItemBlacklistType blacklistType) {
		StackHelper stackHelper = Internal.getStackHelper();
		switch (blacklistType) {
			case ITEM:
				return stackHelper.getUniqueIdentifierForStack(itemStack, StackHelper.UidMode.NORMAL);
			case WILDCARD:
				return stackHelper.getUniqueIdentifierForStack(itemStack, StackHelper.UidMode.WILDCARD);
			case MOD_ID:
				return stackHelper.getModId(itemStack);

		}
		return "";
	}

	public enum ItemBlacklistType {
		ITEM, WILDCARD, MOD_ID;

		public static final ItemBlacklistType[] VALUES = values();
	}
}
