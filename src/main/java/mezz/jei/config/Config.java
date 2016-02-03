package mezz.jei.config;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.Internal;
import mezz.jei.util.StackHelper;

public class Config {
	public static final String CATEGORY_MODE = "mode";
	public static final String CATEGORY_INTERFACE = "interface";
	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	@Deprecated
	public static final String CATEGORY_ADDONS = "addons";

	public static LocalizedConfiguration configFile;
	public static LocalizedConfiguration itemBlacklistFile;

	private static boolean overlayEnabled = true;
	private static boolean cheatItemsEnabled = false;
	private static boolean editModeEnabled = false;
	private static boolean debugModeEnabled = false;

	private static boolean deleteItemsInCheatModeEnabled = true;

	private static boolean jeiOnServer = true;

	private static boolean recipeAnimationsEnabled = true;
	private static boolean hideMissingModelsEnabled = true;

	private static boolean prefixRequiredForModNameSearch = true;
	private static boolean prefixRequiredForTooltipSearch = false;

	private static final Set<String> itemBlacklist = new HashSet<>();

	private static final String[] defaultItemBlacklist = new String[]{};

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return overlayEnabled;
	}

	public static void toggleOverlayEnabled() {
		overlayEnabled = !overlayEnabled;

		Property property = configFile.get(CATEGORY_INTERFACE, "overlayEnabled", overlayEnabled);
		property.set(overlayEnabled);

		if (configFile.hasChanged()) {
			configFile.save();
		}
	}

	public static boolean isCheatItemsEnabled() {
		return cheatItemsEnabled;
	}

	public static boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	public static boolean isDebugModeEnabled() {
		return debugModeEnabled;
	}

	public static boolean isDeleteItemsInCheatModeActive() {
		return deleteItemsInCheatModeEnabled && cheatItemsEnabled && jeiOnServer;
	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void setJeiOnServer(boolean jeiOnServer) {
		Config.jeiOnServer = jeiOnServer;
	}

	public static boolean isRecipeAnimationsEnabled() {
		return recipeAnimationsEnabled;
	}

	public static boolean isHideMissingModelsEnabled() {
		return hideMissingModelsEnabled;
	}

	public static boolean isPrefixRequiredForModNameSearch() {
		return prefixRequiredForModNameSearch;
	}

	public static boolean isPrefixRequiredForTooltipSearch() {
		return prefixRequiredForTooltipSearch;
	}

	public static Set<String> getItemBlacklist() {
		return itemBlacklist;
	}

	public static LocalizedConfiguration getConfigFile() {
		return configFile;
	}

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {
		final String configKeyPrefix = "config.jei";
		final File configurationDir = event.getModConfigurationDirectory();

		configFile = new LocalizedConfiguration(configKeyPrefix, event.getSuggestedConfigurationFile(), "0.2.0");
		itemBlacklistFile = new LocalizedConfiguration(configKeyPrefix, new File(configurationDir, Constants.MOD_ID + "-itemBlacklist.cfg"), "0.1.0");

		syncConfig();
	}

	public static boolean syncConfig() {
		configFile.addCategory(CATEGORY_MODE);
		configFile.addCategory(CATEGORY_INTERFACE);
		configFile.addCategory(CATEGORY_SEARCH);
		configFile.addCategory(CATEGORY_ADVANCED);

		itemBlacklistFile.addCategory(CATEGORY_ADVANCED);

		ConfigCategory addonsCategory = configFile.getCategory("addons");
		if (addonsCategory != null) {
			configFile.removeCategory(addonsCategory);
		}

		overlayEnabled = configFile.getBoolean(CATEGORY_INTERFACE, "overlayEnabled", overlayEnabled);

		cheatItemsEnabled = configFile.getBoolean(CATEGORY_MODE, "cheatItemsEnabled", cheatItemsEnabled);
		editModeEnabled = configFile.getBoolean(CATEGORY_MODE, "editEnabled", editModeEnabled);

		deleteItemsInCheatModeEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "deleteItemsInCheatModeEnabled", deleteItemsInCheatModeEnabled);

		recipeAnimationsEnabled = configFile.getBoolean(CATEGORY_INTERFACE, "recipeAnimationsEnabled", recipeAnimationsEnabled);

		prefixRequiredForModNameSearch = configFile.getBoolean(CATEGORY_SEARCH, "atPrefixRequiredForModName", prefixRequiredForModNameSearch);
		prefixRequiredForTooltipSearch = configFile.getBoolean(CATEGORY_SEARCH, "prefixRequiredForTooltipSearch", prefixRequiredForTooltipSearch);

		ConfigCategory categoryAdvanced = configFile.getCategory(CATEGORY_ADVANCED);
		categoryAdvanced.remove("nbtKeyIgnoreList");

		// migrate item blacklist to new file
		if (configFile.hasKey(CATEGORY_ADVANCED, "itemBlacklist")) {
			Property oldItemBlacklistProperty = configFile.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);
			String[] itemBlacklistArray = oldItemBlacklistProperty.getStringList();
			Property newItemBlacklistProperty = itemBlacklistFile.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);
			newItemBlacklistProperty.set(itemBlacklistArray);
			categoryAdvanced.remove("itemBlacklist");
		}

		String[] itemBlacklistArray = itemBlacklistFile.getStringList("itemBlacklist", CATEGORY_ADVANCED, defaultItemBlacklist);
		itemBlacklist.clear();
		Collections.addAll(itemBlacklist, itemBlacklistArray);

		hideMissingModelsEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "hideMissingModelsEnabled", hideMissingModelsEnabled);

		debugModeEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
		{
			Property property = configFile.get(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
			property.setShowInGui(false);
		}

		final boolean configChanged = configFile.hasChanged();
		if (configChanged) {
			configFile.save();
		}

		final boolean itemBlacklistChanged = itemBlacklistFile.hasChanged();
		if (itemBlacklistChanged) {
			itemBlacklistFile.save();
		}

		return configChanged || itemBlacklistChanged;
	}

	private static void updateBlacklist() {
		Property property = itemBlacklistFile.get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);

		String[] currentBlacklist = itemBlacklist.toArray(new String[itemBlacklist.size()]);
		property.set(currentBlacklist);

		if (itemBlacklistFile.hasChanged()) {
			itemBlacklistFile.save();
		}
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
				return stackHelper.getUniqueIdentifierForStack(itemStack, false);
			case WILDCARD:
				return stackHelper.getUniqueIdentifierForStack(itemStack, true);
			case MOD_ID:
				return stackHelper.getModIdForStack(itemStack);

		}
		return "";
	}

	public enum ItemBlacklistType {
		ITEM, WILDCARD, MOD_ID;

		public static final ItemBlacklistType[] VALUES = values();
	}
}
