package mezz.jei.fabric.input;

import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;

public class FabricJeiKeyMappingCategoryBuilder implements IJeiKeyMappingCategoryBuilder {
    private final String category;

    public FabricJeiKeyMappingCategoryBuilder(String category) {
        this.category = category;
    }

    @Override
    public IJeiKeyMappingBuilder createMapping(String description) {
        return new FabricJeiKeyMappingBuilder(category, description);
    }
}
