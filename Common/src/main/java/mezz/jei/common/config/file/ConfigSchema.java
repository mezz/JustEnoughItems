package mezz.jei.common.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigSchema {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Path path;
    @Unmodifiable
    private final Map<String, ConfigCategory> categories;
    private final AtomicBoolean needsLoad = new AtomicBoolean(true);

    public ConfigSchema(Path path, List<ConfigCategoryBuilder> categoryBuilders) {
        this.path = path;
        Map<String, ConfigCategory> map = new LinkedHashMap<>();
        for (ConfigCategoryBuilder builder : categoryBuilders) {
            ConfigCategory category = builder.build(this);
            map.put(category.getName(), category);
        }
        this.categories = Collections.unmodifiableMap(map);
    }

    public void loadIfNeeded() {
        if (!needsLoad.get()) {
            return;
        }
        needsLoad.set(false);

        if (Files.exists(path)) {
            try {
                ConfigSerializer.load(this);
            } catch (IOException e) {
                LOGGER.error("Failed to load config schema for: %s".formatted(path), e);
            }
        }
    }

    public void onFileChanged() {
        needsLoad.set(true);
    }

    public Path getPath() {
        return path;
    }

    @Nullable
    public ConfigCategory getCategory(String categoryName) {
        return this.categories.get(categoryName);
    }

    public Collection<ConfigCategory> getCategories() {
        return this.categories.values();
    }

    public Set<String> getCategoryNames() {
        return this.categories.keySet();
    }
}
