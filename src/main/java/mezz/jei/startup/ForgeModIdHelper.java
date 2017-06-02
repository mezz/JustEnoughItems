package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.Map;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ForgeModIdHelper extends AbstractModIdHelper {
	@Nullable
	private static ForgeModIdHelper INSTANCE;

	public static IModIdHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ForgeModIdHelper();
		}
		return INSTANCE;
	}

	private final Map<String, ModContainer> modMap;

	private ForgeModIdHelper() {
		this.modMap = Loader.instance().getIndexedModList();
	}

	@Override
	public String getModNameForModId(String modId) {
		ModContainer modContainer = this.modMap.get(modId);
		if (modContainer == null) {
			return modId;
		}
		return modContainer.getName();
	}
}
