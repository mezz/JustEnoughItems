package mezz.jei.config;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.util.StackUtil;

public class Config {
	public static LocalizedConfiguration configFile;

	public static final String categoryMode = "mode";
	public static final String categoryInterface = "interface";
	public static final String categoryAdvanced = "advanced";
	public static final String categoryAddons = "addons";

	public static boolean cheatItemsEnabled = false;
	public static boolean editModeEnabled = false;
	public static boolean recipeTransferEnabled = true;
	public static boolean recipeAnimationsEnabled = true;

	public static Set<String> nbtKeyIgnoreList = new HashSet<>();
	public static Set<String> itemBlacklist = new HashSet<>();

	public static final String[] defaultItemBlacklist = new String[]{};
	public static final String[] defaultNbtKeyIgnoreList = new String[]{"BlockEntityTag", "CanPlaceOn"};

	public static void preInit(@Nonnull FMLPreInitializationEvent event) {
		configFile = new LocalizedConfiguration("config.jei", event.getSuggestedConfigurationFile(), "0.1.0");

		syncConfig();
	}

	public static boolean syncConfig() {
		configFile.addCategory(categoryMode);
		configFile.addCategory(categoryInterface);
		configFile.addCategory(categoryAdvanced);
		configFile.addCategory(categoryAddons);

		cheatItemsEnabled = configFile.getBoolean(categoryMode, "cheatItemsEnabled", cheatItemsEnabled);
		editModeEnabled = configFile.getBoolean(categoryMode, "editEnabled", editModeEnabled);

		recipeAnimationsEnabled = configFile.getBoolean(categoryInterface, "recipeAnimationsEnabled", recipeAnimationsEnabled);

		String[] nbtKeyIgnoreListArray = configFile.getStringList("nbtKeyIgnoreList", categoryAdvanced, defaultNbtKeyIgnoreList);
		nbtKeyIgnoreList.clear();
		Collections.addAll(nbtKeyIgnoreList, nbtKeyIgnoreListArray);

		String[] itemBlacklistArray = configFile.getStringList("itemBlacklist", categoryAdvanced, defaultItemBlacklist);
		itemBlacklist.clear();
		Collections.addAll(itemBlacklist, itemBlacklistArray);

		boolean configChanged = configFile.hasChanged();
		if (configChanged) {
			configFile.save();
		}
		return configChanged;
	}

	private static void updateBlacklist() {
		Property property = configFile.getConfiguration().get(categoryAdvanced, "itemBlacklist", defaultItemBlacklist);

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
		String uid = StackUtil.getUniqueIdentifierForStack(itemStack, wildcard);
		if (itemBlacklist.add(uid)) {
			updateBlacklist();
		}
	}

	public static void removeItemFromConfigBlacklist(ItemStack itemStack, boolean wildcard) {
		if (itemStack == null) {
			return;
		}
		String uid = StackUtil.getUniqueIdentifierForStack(itemStack, wildcard);
		if (itemBlacklist.remove(uid)) {
			updateBlacklist();
		}
	}

	public static boolean isItemOnConfigBlacklist(ItemStack itemStack, boolean wildcard) {
		String uid = StackUtil.getUniqueIdentifierForStack(itemStack, wildcard);
		return itemBlacklist.contains(uid);
	}
}
