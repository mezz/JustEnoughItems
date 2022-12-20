package mezz.jei.forge.input;

import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;

public class ForgeJeiKeyMappingCategoryBuilder implements IJeiKeyMappingCategoryBuilder {
    private final String category;

    public ForgeJeiKeyMappingCategoryBuilder(String category) {
        this.category = category;
    }

    @Override
    public IJeiKeyMappingBuilder createMapping(String description) {
        return new ForgeJeiKeyMappingBuilder(category, description);
    }
}
