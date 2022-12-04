package mezz.jei.core.config.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigSchemaBuilder implements IConfigSchemaBuilder {
    private final Set<String> categoryNames = new HashSet<>();
    private final List<ConfigCategoryBuilder> categoryBuilders = new ArrayList<>();
    private final Path configFile;

    public ConfigSchemaBuilder(Path configFile) {
        this.configFile = configFile;
    }

    @Override
    public IConfigCategoryBuilder addCategory(String name) {
        if (!categoryNames.add(name)) {
            throw new IllegalArgumentException("There is already a category named: " + name);
        }
        ConfigCategoryBuilder category = new ConfigCategoryBuilder(name);
        this.categoryBuilders.add(category);
        return category;
    }

    @Override
    public IConfigSchema build() {
        return new ConfigSchema(configFile, categoryBuilders);
    }
}
