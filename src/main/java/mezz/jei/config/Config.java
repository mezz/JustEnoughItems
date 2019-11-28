package mezz.jei.config;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TextFormatting;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.GiveMode;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

public final class Config {
	private static final String configKeyPrefix = "config.jei";

	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	public static final String defaultModNameFormatFriendly = "blue italic";
	public static final int smallestNumColumns = 4;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;
	public static final int maxRecipeGuiHeight = 5000;

	@Nullable
	private static LocalizedConfiguration config;
	@Nullable
	private static Configuration worldConfig;
	@Nullable
	private static LocalizedConfiguration itemBlacklistConfig;
	@Nullable
	private static LocalizedConfiguration searchColorsConfig;
	@Nullable
	private static File bookmarkFile;

	private static final ConfigValues defaultValues = new ConfigValues();
	private static final ConfigValues values = new ConfigValues();
	@Nullable
	private static String modNameFormatOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	// item blacklist
	private static final Set<String> itemBlacklist = new HashSet<>();
	private static final String[] defaultItemBlacklist = new String[]{};

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return values.overlayEnabled ||
			KeyBindings.toggleOverlay.getKeyCode() == 0; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	public static void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		if (worldConfig != null) {
			NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new OverlayToggleEvent(values.overlayEnabled));
	}

	public static boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && values.bookmarkOverlayEnabled;
	}

	public static void toggleBookmarkEnabled() {
		values.bookmarkOverlayEnabled = !values.bookmarkOverlayEnabled;

		if (worldConfig != null) {
			NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
			property.set(values.bookmarkOverlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new BookmarkOverlayToggleEvent(values.bookmarkOverlayEnabled));
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
				NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
				final String worldCategory = ServerInfo.getWorldUid(networkManager);
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
				}
			}

			if (values.cheatItemsEnabled && ServerInfo.isJeiOnServer()) {
				JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	public static boolean isEditModeEnabled() {
		return values.editModeEnabled;
	}

	public static void toggleEditModeEnabled() {
		values.editModeEnabled = !values.editModeEnabled;
		if (worldConfig != null) {
			NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "editEnabled", defaultValues.editModeEnabled);
			property.set(values.editModeEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new EditModeToggleEvent(values.editModeEnabled));
	}

	public static boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return values.cheatItemsEnabled && ServerInfo.isJeiOnServer();
	}

	public static boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	public static boolean isOptimizeMemoryUsage() {
		return values.optimizeMemoryUsage;
	}

	public static GiveMode getGiveMode() {
		return values.giveMode;
	}

	public static String getModNameFormat() {
		String override = Config.modNameFormatOverride;
		if (override != null) {
			return override;
		}
		return values.modNameFormat;
	}

	public static boolean isModNameFormatOverrideActive() {
		return Config.modNameFormatOverride != null;
	}

	public static void checkForModNameFormatOverride() {
		IModIdHelper modIdHelper = ForgeModIdHelper.getInstance();
		Config.modNameFormatOverride = modIdHelper.getModNameTooltipFormatting();
		if (config != null) {
			updateModNameFormat(config);
		}
	}

	public static int getMaxColumns() {
		return values.maxColumns;
	}

	public static int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight;
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
			NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
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

	@Nullable
	public static File getBookmarkFile() {
		return bookmarkFile;
	}

	public static void preInit(FMLPreInitializationEvent event) {

		File jeiConfigurationDir = new File(event.getModConfigurationDirectory(), Constants.MOD_ID);
		if (!jeiConfigurationDir.exists()) {
			try {
				if (!jeiConfigurationDir.mkdir()) {
					Log.get().error("Could not create config directory {}", jeiConfigurationDir);
					return;
				}
			} catch (SecurityException e) {
				Log.get().error("Could not create config directory {}", jeiConfigurationDir, e);
				return;
			}
		}

		final File configFile = new File(jeiConfigurationDir, "jei.cfg");
		final File itemBlacklistConfigFile = new File(jeiConfigurationDir, "itemBlacklist.cfg");
		final File searchColorsConfigFile = new File(jeiConfigurationDir, "searchColors.cfg");
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		bookmarkFile = new File(jeiConfigurationDir, "bookmarks.ini");
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
		categoryAdvanced.remove("maxSubtypes");

		values.centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", defaultValues.centerSearchBarEnabled);

		values.optimizeMemoryUsage = config.getBoolean(CATEGORY_ADVANCED, "optimizeMemoryUsage", defaultValues.optimizeMemoryUsage);

		values.giveMode = config.getEnum("giveMode", CATEGORY_ADVANCED, defaultValues.giveMode, GiveMode.values());

		values.maxColumns = config.getInt("maxColumns", CATEGORY_ADVANCED, defaultValues.maxColumns, smallestNumColumns, largestNumColumns);

		values.maxRecipeGuiHeight = config.getInt("maxRecipeGuiHeight", CATEGORY_ADVANCED, defaultValues.maxRecipeGuiHeight, minRecipeGuiHeight, maxRecipeGuiHeight);

		updateModNameFormat(config);

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

	private static void updateModNameFormat(LocalizedConfiguration config) {
		EnumSet<TextFormatting> validFormatting = EnumSet.allOf(TextFormatting.class);
		validFormatting.remove(TextFormatting.RESET);
		String[] validValues = new String[validFormatting.size()];
		int i = 0;
		for (TextFormatting formatting : validFormatting) {
			validValues[i] = formatting.getFriendlyName().toLowerCase(Locale.ENGLISH);
			i++;
		}
		Property property = config.getString("modNameFormat", CATEGORY_ADVANCED, defaultModNameFormatFriendly, validValues);
		boolean showInGui = !isModNameFormatOverrideActive();
		property.setShowInGui(showInGui);
		String modNameFormatFriendly = property.getString();
		values.modNameFormat = parseFriendlyModNameFormat(modNameFormatFriendly);
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
				Log.get().error("Invalid format: {}", string);
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

	public static boolean syncWorldConfig(@Nullable NetworkManager networkManager) {
		if (worldConfig == null) {
			return false;
		}

		final String worldCategory = ServerInfo.getWorldUid(networkManager);

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

		property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
		property.setLanguageKey("config.jei.interface.bookmarkOverlayEnabled");
		property.setComment(Translator.translateToLocal("config.jei.interface.bookmarkOverlayEnabled.comment"));
		property.setShowInGui(false);
		values.bookmarkOverlayEnabled = property.getBoolean();

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
				Log.get().error("Invalid format for searchColor entry: {}", entry);
			} else {
				try {
					final String name = values[0];
					final int colorValue = Integer.decode("0x" + values[1]);
					final Color color = new Color(colorValue);
					searchColorsMapBuilder.put(color, name);
				} catch (NumberFormatException e) {
					Log.get().error("Invalid number format for searchColor entry: {}", entry, e);
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
