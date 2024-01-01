package mezz.jei.fabric.input;

import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.fabricmc.loader.api.FabricLoader;

public class FabricJeiKeyMappingCategoryBuilder implements IJeiKeyMappingCategoryBuilder {
	private final String category;

	public FabricJeiKeyMappingCategoryBuilder(String category) {
		this.category = category;
	}

	@Override
	public IJeiKeyMappingBuilder createMapping(String description) {
		if (FabricLoader.getInstance().isModLoaded("amecsapi")) {
			return new AmecsJeiKeyMappingBuilder(category, description);
		} else {
			return new FabricJeiKeyMappingBuilder(category, description);
		}
	}
}
