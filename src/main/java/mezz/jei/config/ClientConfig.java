package mezz.jei.config;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.util.EnumSet;
import java.util.Locale;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TextFormatting;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.color.ColorGetter;
import mezz.jei.color.ColorNamer;
import mezz.jei.events.BookmarkOverlayToggleEvent;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.GiveMode;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import org.lwjgl.glfw.GLFW;

public final class ClientConfig implements IIngredientFilterConfig, IFilterTextSource {
	@Nullable
	private static ClientConfig instance;
	private static final String configKeyPrefix = "config.jei";

	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_SEARCH_COLORS = "searchColors";

	public static final String defaultModNameFormatFriendly = "blue italic";
	public static final int smallestNumColumns = 4;
	public static final int largestNumColumns = 100;
	public static final int minRecipeGuiHeight = 175;
	public static final int maxRecipeGuiHeight = 5000;

	private final LocalizedConfiguration config;
	private final Configuration worldConfig;
	private final LocalizedConfiguration searchColorsConfig;
	private final File bookmarkFile;

	private final ConfigValues defaultValues = new ConfigValues();
	private final ConfigValues values = new ConfigValues();
	@Nullable
	private String modNameFormatOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	@Deprecated
	public static ClientConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
	}

	public ClientConfig(File jeiConfigurationDir) {
		instance = this;
		final File configFile = new File(jeiConfigurationDir, "jei.cfg");
		final File searchColorsConfigFile = new File(jeiConfigurationDir, "searchColors.cfg");
		// TODO move world settings into the world save folder
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		this.bookmarkFile = new File(jeiConfigurationDir, "bookmarks.ini");
		this.worldConfig = new Configuration(worldConfigFile, "0.1.0");
		this.config = new LocalizedConfiguration(configKeyPrefix, configFile, "0.4.0");
		this.searchColorsConfig = new LocalizedConfiguration(configKeyPrefix, searchColorsConfigFile, "0.1.0");
	}

	public void onWorldSave() {
		try {
			saveFilterText();
		} catch (RuntimeException e) {
			Log.get().error("Failed to save filter text.", e);
		}
	}

	public void onConfigChanged(String modId) {
		if (Constants.MOD_ID.equals(modId)) {
			if (syncAllConfig()) {
				JeiRuntime runtime = Internal.getRuntime();
				if (runtime != null) {
					IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
					ingredientListOverlay.rebuildItemFilter();
				}
			}
		} else {
			checkForModNameFormatOverride();
		}
	}

	public boolean isOverlayEnabled() {
		return values.overlayEnabled ||
			KeyBindings.toggleOverlay.getKey().getKeyCode() == GLFW.GLFW_KEY_UNKNOWN; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	public void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//				worldConfig.save();
			}
		}

		EventBusHelper.post(new OverlayToggleEvent(values.overlayEnabled));
	}

	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && values.bookmarkOverlayEnabled;
	}

	public void toggleBookmarkEnabled() {
		values.bookmarkOverlayEnabled = !values.bookmarkOverlayEnabled;

		NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
			property.set(values.bookmarkOverlayEnabled);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//				worldConfig.save();
			}
		}

		EventBusHelper.post(new BookmarkOverlayToggleEvent(values.bookmarkOverlayEnabled));
	}

	public boolean isCheatItemsEnabled() {
		return values.cheatItemsEnabled;
	}

	public void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!values.cheatItemsEnabled);
	}

	public void setCheatItemsEnabled(boolean value) {
		if (values.cheatItemsEnabled != value) {
			values.cheatItemsEnabled = value;

			NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				final String worldCategory = ServerInfo.getWorldUid(networkManager);
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					// TODO 1.13
//					worldConfig.save();
				}
			}

			if (values.cheatItemsEnabled && ServerInfo.isJeiOnServer()) {
				Network.sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	public boolean isHideModeEnabled() {
		return values.hideModeEnabled;
	}

	public boolean isDebugModeEnabled() {
		return values.debugModeEnabled;
	}

	public boolean isDeleteItemsInCheatModeActive() {
		return values.cheatItemsEnabled && ServerInfo.isJeiOnServer();
	}

	public boolean isCenterSearchBarEnabled() {
		return values.centerSearchBarEnabled;
	}

	public GiveMode getGiveMode() {
		return values.giveMode;
	}

	public String getModNameFormat() {
		String override = modNameFormatOverride;
		if (override != null) {
			return override;
		}
		return values.modNameFormat;
	}

	public boolean isModNameFormatOverrideActive() {
		return modNameFormatOverride != null;
	}

	public void checkForModNameFormatOverride() {
		IModIdHelper modIdHelper = ForgeModIdHelper.getInstance();
		modNameFormatOverride = modIdHelper.getModNameTooltipFormatting();
		updateModNameFormat(config);
	}

	public int getMaxColumns() {
		return values.maxColumns;
	}

	public int getMaxRecipeGuiHeight() {
		return values.maxRecipeGuiHeight;
	}

	@Override
	public SearchMode getModNameSearchMode() {
		return values.modNameSearchMode;
	}

	@Override
	public SearchMode getTooltipSearchMode() {
		return values.tooltipSearchMode;
	}

	@Override
	public SearchMode getOreDictSearchMode() {
		return values.oreDictSearchMode;
	}

	@Override
	public SearchMode getCreativeTabSearchMode() {
		return values.creativeTabSearchMode;
	}

	@Override
	public SearchMode getColorSearchMode() {
		return values.colorSearchMode;
	}

	@Override
	public SearchMode getResourceIdSearchMode() {
		return values.resourceIdSearchMode;
	}

	@Override
	public boolean getSearchAdvancedTooltips() {
		return values.searchAdvancedTooltips;
	}

	public boolean setFilterText(String filterText) {
		if (values.filterText.equals(filterText)) {
			return false;
		} else {
			values.filterText = filterText;
			return true;
		}
	}

	@Override
	public String getFilterText() {
		return values.filterText;
	}

	public void saveFilterText() {
		NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
			property.set(values.filterText);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//				worldConfig.save();
			}
		}
	}

	public LocalizedConfiguration getConfig() {
		return config;
	}

	public Configuration getWorldConfig() {
		return worldConfig;
	}

	public File getBookmarkFile() {
		return bookmarkFile;
	}

	public void onPreInit() {
		syncConfig();
		syncSearchColorsConfig();
	}

	private boolean syncAllConfig() {
		boolean needsReload = false;
		if (syncConfig()) {
			needsReload = true;
		}

		NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			if (syncWorldConfig(networkManager)) {
				needsReload = true;
			}
		}

		if (syncSearchColorsConfig()) {
			needsReload = true;
		}

		return needsReload;
	}

	private boolean syncConfig() {
		boolean needsReload = false;

		config.addCategory(CATEGORY_SEARCH);
		config.addCategory(CATEGORY_ADVANCED);

		SearchMode[] searchModes = SearchMode.values();

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

		values.centerSearchBarEnabled = config.getBoolean(CATEGORY_ADVANCED, "centerSearchBarEnabled", defaultValues.centerSearchBarEnabled);

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
			// TODO 1.13
//			config.save();
		}
		return needsReload;
	}

	private void updateModNameFormat(LocalizedConfiguration config) {
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

	public boolean syncWorldConfig(@Nullable NetworkManager networkManager) {

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

		property = worldConfig.get(worldCategory, "editEnabled", defaultValues.hideModeEnabled);
		property.setLanguageKey("config.jei.mode.editEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.editEnabled.comment"));
		values.hideModeEnabled = property.getBoolean();
		if (property.hasChanged()) {
			EventBusHelper.post(new EditModeToggleEvent(values.hideModeEnabled));
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
			// TODO 1.13
//			worldConfig.save();
		}
		return false;
	}

	private boolean syncSearchColorsConfig() {
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
			// TODO 1.13
//			searchColorsConfig.save();
		}
		return configChanged;
	}
}
