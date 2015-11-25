package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.item.Item;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModList {

	private final Map<String, String> modNamesForIds = new HashMap<>();

	public ModList() {
		Map<String,ModContainer> modMap = Loader.instance().getIndexedModList();
		for (Map.Entry<String, ModContainer> modEntry : modMap.entrySet()) {
			String lowercaseId = modEntry.getKey().toLowerCase(Locale.ENGLISH);
			String modName = modEntry.getValue().getName();
			modNamesForIds.put(lowercaseId, modName);
		}
	}

	@Nonnull
	public String getModNameForItemStack(@Nonnull Item item) {
		String modId = GameRegistry.findUniqueIdentifierFor(item).modId;
		String modName = modNamesForIds.get(modId.toLowerCase(Locale.ENGLISH));
		if (modName == null) {
			modName = WordUtils.capitalize(modId);
		}
		return modName;
	}
}
