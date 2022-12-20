package mezz.jei.core.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigSchema implements IConfigSchema {
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

    @Override
    public void loadIfNeeded() {
        if (!needsLoad.get()) {
            return;
        }
        needsLoad.set(false);

        if (Files.exists(path)) {
            try {
                ConfigSerializer.load(path, categories);
            } catch (IOException e) {
                LOGGER.error("Failed to load config schema for: %s".formatted(path), e);
            }
        }
    }

    private void onFileChanged() {
        needsLoad.set(true);
    }

    @Override
    public void register() {
        if (!Files.exists(path)) {
            try {
                ConfigSerializer.save(path, categories.values());
            } catch (IOException e) {
                LOGGER.error("Failed to create config file: '{}'", path, e);
            }
        }

        try {
            Map<Path, Runnable> callbacks = Map.of(path, this::onFileChanged);
            FileWatcher fileWatcher = new FileWatcher(callbacks);
            Thread thread = new Thread(fileWatcher::run, "JEI Config file watcher");
            thread.start();
        } catch (IOException e) {
            LOGGER.error("Failed to create FileWatcher Thread for config file: '{}'", path, e);
        }
    }
}
