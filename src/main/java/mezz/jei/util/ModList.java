package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.text.WordUtils;

public class ModList {

	private final Map<String, String> modNamesForIds = new HashMap<String, String>();

	public ModList() {
		Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
		for (Map.Entry<String, ModContainer> modEntry : modMap.entrySet()) {
			String lowercaseId = modEntry.getKey().toLowerCase(Locale.ENGLISH);
			String modName = modEntry.getValue().getName();
			modNamesForIds.put(lowercaseId, modName);
		}
	}

	@Nonnull
	public String getModNameForItem(@Nonnull Item item) {
		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
			throw new NullPointerException("item.getRegistryName() returned null for: " + stackInfo);
		}
		String modId = itemResourceLocation.getResourceDomain();
		return getModNameForModId(modId);
	}

	@Nonnull
	public String getModNameForModId(@Nonnull String modId) {
		String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
		String modName = modNamesForIds.get(lowercaseModId);
		if (modName == null) {
			modName = WordUtils.capitalize(modId);
			modNamesForIds.put(lowercaseModId, modName);
		}
		return modName;
	}
}
