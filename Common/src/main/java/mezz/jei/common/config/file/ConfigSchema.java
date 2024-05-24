package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.ConfigManager;
import mezz.jei.common.util.DeduplicatingRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigSchema implements IConfigSchema {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration SAVE_DELAY_TIME = Duration.ofSeconds(2);

	private final Path path;
	@Unmodifiable
	private final List<ConfigCategory> categories;
	private final AtomicBoolean needsLoad = new AtomicBoolean(true);
	private final DeduplicatingRunner delayedSave = new DeduplicatingRunner(this::save, SAVE_DELAY_TIME, "JEI Config Save Delay");

	public ConfigSchema(Path path, List<ConfigCategoryBuilder> categoryBuilders) {
		this.path = path;
		this.categories = categoryBuilders.stream()
			.map(b -> b.build(this))
			.toList();
	}

	@Override
	public void loadIfNeeded() {
		if (!needsLoad.compareAndSet(true, false)) {
			return;
		}

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
	public void register(FileWatcher fileWatcher, ConfigManager configManager) {
		if (Files.exists(path)) {
			loadIfNeeded();
		}
		save();

		fileWatcher.addCallback(path, this::onFileChanged);
		configManager.registerConfigFile(this);
	}

	private void save() {
		try {
			ConfigSerializer.save(path, categories);
		} catch (IOException e) {
			LOGGER.error("Failed to save config file: '{}'", path, e);
		}
	}

	@Override
	public void markDirty() {
		delayedSave.run();
	}

	@Override
	@Unmodifiable
	public List<ConfigCategory> getCategories() {
		return categories;
	}

	@Override
	public Optional<? extends IJeiConfigCategory> getCategory(String categoryName) {
		ConfigCategory found = null;
		for (ConfigCategory category : categories) {
			if (category.getName().equals(categoryName)) {
				found = category;
				break;
			}
		}
		return Optional.ofNullable(found);
	}

	@Override
	public Optional<? extends IJeiConfigValue<?>> getConfigValue(String categoryName, String valueName) {
		var cat = this.getCategory(categoryName);

		if (cat.isEmpty()) {
			return Optional.ofNullable(null);
		}

		return cat.get().getConfigValue(valueName);

	}

	@Override
	public Path getPath() {
		return path;
	}
}
