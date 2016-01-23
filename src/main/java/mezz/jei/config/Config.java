package mezz.jei.config;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.Internal;

public class Config {
	public static final String CATEGORY_MODE = "mode";
	public static final String CATEGORY_INTERFACE = "interface";
	public static final String CATEGORY_SEARCH = "search";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_ADDONS = "addons";

	public static LocalizedConfiguration configFile;

	private static boolean overlayEnabled = true;
	private static boolean cheatItemsEnabled = false;
	private static boolean editModeEnabled = false;
	private static boolean debugModeEnabled = false;

	private static boolean deleteItemsInCheatModeEnabled = true;

	private static boolean jeiOnServer = true;

	private static boolean recipeAnimationsEnabled = true;
	private static boolean hideMissingModelsEnabled = true;

	private static boolean atPrefixRequiredForModName = true;

	private static final Set<String> nbtKeyIgnoreList = new HashSet<>();
	private static final Set<String> itemBlacklist = new HashSet<>();

	private static final String[] defaultItemBlacklist = new String[]{};
	private static final String[] defaultNbtKeyIgnoreList = new String[]{"BlockEntityTag", "CanPlaceOn"};

	private Config() {

	}

	public static boolean isOverlayEnabled() {
		return overlayEnabled;
	}

	public static void toggleOverlayEnabled() {
		overlayEnabled = !overlayEnabled;

		Property property = configFile.getConfiguration().get(CATEGORY_INTERFACE, "overlayEnabled", overlayEnabled);
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

	public static boolean isAtPrefixRequiredForModName() {
		return atPrefixRequiredForModName;
	}

	public static Set<String> getNbtKeyIgnoreList() {
		return nbtKeyIgnoreList;
	}

	public static Set<String> getItemBlacklist() {
		return itemBlacklist;
	}

	public static LocalizedConfiguration getConfigFile() {
		return configFile;
	}

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {
		configFile = new LocalizedConfiguration("config.jei", event.getSuggestedConfigurationFile(), "0.1.0");

		syncConfig();
	}

	public static boolean syncConfig() {
		configFile.addCategory(CATEGORY_MODE);
		configFile.addCategory(CATEGORY_INTERFACE);
		configFile.addCategory(CATEGORY_SEARCH);
		configFile.addCategory(CATEGORY_ADVANCED);
		configFile.addCategory(CATEGORY_ADDONS);

		overlayEnabled = configFile.getBoolean(CATEGORY_INTERFACE, "overlayEnabled", overlayEnabled);

		cheatItemsEnabled = configFile.getBoolean(CATEGORY_MODE, "cheatItemsEnabled", cheatItemsEnabled);
		editModeEnabled = configFile.getBoolean(CATEGORY_MODE, "editEnabled", editModeEnabled);

		deleteItemsInCheatModeEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "deleteItemsInCheatModeEnabled", deleteItemsInCheatModeEnabled);

		recipeAnimationsEnabled = configFile.getBoolean(CATEGORY_INTERFACE, "recipeAnimationsEnabled", recipeAnimationsEnabled);

		atPrefixRequiredForModName = configFile.getBoolean(CATEGORY_SEARCH, "atPrefixRequiredForModName", atPrefixRequiredForModName);

		String[] nbtKeyIgnoreListArray = configFile.getStringList("nbtKeyIgnoreList", CATEGORY_ADVANCED, defaultNbtKeyIgnoreList);
		nbtKeyIgnoreList.clear();
		Collections.addAll(nbtKeyIgnoreList, nbtKeyIgnoreListArray);

		String[] itemBlacklistArray = configFile.getStringList("itemBlacklist", CATEGORY_ADVANCED, defaultItemBlacklist);
		itemBlacklist.clear();
		Collections.addAll(itemBlacklist, itemBlacklistArray);
		{
			Property property = configFile.getConfiguration().get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);
			property.setShowInGui(false);
		}

		hideMissingModelsEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "hideMissingModelsEnabled", hideMissingModelsEnabled);

		debugModeEnabled = configFile.getBoolean(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
		{
			Property property = configFile.getConfiguration().get(CATEGORY_ADVANCED, "debugModeEnabled", debugModeEnabled);
			property.setShowInGui(false);
		}

		boolean configChanged = configFile.hasChanged();
		if (configChanged) {
			configFile.save();
		}
		return configChanged;
	}

	private static void updateBlacklist() {
		Property property = configFile.getConfiguration().get(CATEGORY_ADVANCED, "itemBlacklist", defaultItemBlacklist);

		String[] currentBlacklist = itemBlacklist.toArray(new String[itemBlacklist.size()]);
		property.set(currentBlacklist);

		if (configFile.hasChanged()) {
			configFile.save();
		}
	}

	public static void addItemToConfigBlacklist(ItemStack itemStack, boolean wildcard) {
		if (itemStack == null) {
			return;
		}
		String uid = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack, wildcard);
		if (itemBlacklist.add(uid)) {
			updateBlacklist();
		}
	}

	public static void removeItemFromConfigBlacklist(ItemStack itemStack, boolean wildcard) {
		if (itemStack == null) {
			return;
		}
		String uid = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack, wildcard);
		if (itemBlacklist.remove(uid)) {
			updateBlacklist();
		}
	}

	public static boolean isItemOnConfigBlacklist(ItemStack itemStack, boolean wildcard) {
		String uid = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack, wildcard);
		return itemBlacklist.contains(uid);
	}
}
