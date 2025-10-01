package mezz.jei.neoforge.input;

import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;

public class ForgeJeiKeyMappingCategoryBuilder implements IJeiKeyMappingCategoryBuilder {
	private final KeyMapping.Category category;

	public ForgeJeiKeyMappingCategoryBuilder(KeyMapping.Category category) {
		this.category = category;
	}

	@Override
	public IJeiKeyMappingBuilder createMapping(String description) {
		return new ForgeJeiKeyMappingBuilder(category, description);
	}
}
