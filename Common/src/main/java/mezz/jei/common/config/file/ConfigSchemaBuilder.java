package mezz.jei.common.config.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigSchemaBuilder {
    private final Set<String> categoryNames = new HashSet<>();
    private final List<ConfigCategoryBuilder> categoryBuilders = new ArrayList<>();

    public ConfigCategoryBuilder addCategory(String name) {
        if (!categoryNames.add(name)) {
            throw new IllegalArgumentException("There is already a category named: " + name);
        }
        ConfigCategoryBuilder category = new ConfigCategoryBuilder(name);
        this.categoryBuilders.add(category);
        return category;
    }

    public ConfigSchema build(Path path) {
        return new ConfigSchema(path, categoryBuilders);
    }
}
