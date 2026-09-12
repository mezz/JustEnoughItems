package mezz.jei.common.config;

import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.schema.category.IConfigCategory;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.value.editor.IConfigValueEditorInfo;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Adapts the standalone config API to JEI's deprecated config API.
 */
@SuppressWarnings({"removal"})
public final class ConfigManagerAdapter {
	private ConfigManagerAdapter() {

	}

	public static mezz.jei.api.runtime.config.IJeiConfigManager create() {
		return create(listenerRemoval -> {});
	}

	public static mezz.jei.api.runtime.config.IJeiConfigManager create(
		Consumer<Runnable> listenerRemovalRegistrar
	) {
		return create(Configs::getSchemas, listenerRemovalRegistrar);
	}

	public static mezz.jei.api.runtime.config.IJeiConfigManager create(
		Supplier<? extends Collection<? extends IConfigSchema>> schemas
	) {
		return create(schemas, listenerRemoval -> {});
	}

	public static mezz.jei.api.runtime.config.IJeiConfigManager create(
		Supplier<? extends Collection<? extends IConfigSchema>> schemas,
		Consumer<Runnable> listenerRemovalRegistrar
	) {
		return new ConfigManager(
			Objects.requireNonNull(schemas),
			Objects.requireNonNull(listenerRemovalRegistrar)
		);
	}

	private static mezz.jei.api.runtime.config.IJeiConfigValue<?> createConfigValue(
		IConfigValue<?> configValue,
		Consumer<Runnable> listenerRemovalRegistrar
	) {
		return createTypedConfigValue(configValue, listenerRemovalRegistrar);
	}

	private static <T> mezz.jei.api.runtime.config.IJeiConfigValue<T> createTypedConfigValue(
		IConfigValue<T> configValue,
		Consumer<Runnable> listenerRemovalRegistrar
	) {
		return new ConfigValue<>(configValue, listenerRemovalRegistrar);
	}

	private static <T> mezz.jei.api.runtime.config.IJeiConfigValueSerializer<T> createSerializer(
		IConfigValueSerializer<T> serializer
	) {
		return new ConfigValueSerializer<>(serializer);
	}

	private static Component getLocalizedName(String localizationKey) {
		return Component.translatable(localizationKey);
	}

	private static Component getLocalizedDescription(String localizationKey) {
		return Component.translatable(localizationKey + ".description");
	}

	private record ConfigManager(
		Supplier<? extends Collection<? extends IConfigSchema>> schemas,
		Consumer<Runnable> listenerRemovalRegistrar
	) implements mezz.jei.api.runtime.config.IJeiConfigManager {
		@Override
		@Unmodifiable
		public Collection<mezz.jei.api.runtime.config.IJeiConfigFile> getConfigFiles() {
			return schemas.get()
				.stream()
				.map(configFile -> (mezz.jei.api.runtime.config.IJeiConfigFile) new ConfigFile(configFile, listenerRemovalRegistrar))
				.toList();
		}
	}

	private record ConfigFile(
		IConfigSchema delegate,
		Consumer<Runnable> listenerRemovalRegistrar
	) implements mezz.jei.api.runtime.config.IJeiConfigFile {
		@Override
		public Path getPath() {
			return delegate.getPath()
				.orElseThrow(() -> new IllegalStateException("Config schema is inactive: %s".formatted(delegate.getModId())));
		}

		@Override
		@Unmodifiable
		public List<? extends mezz.jei.api.runtime.config.IJeiConfigCategory> getCategories() {
			return delegate.getCategories()
				.stream()
				.map(category -> new ConfigCategory(category, listenerRemovalRegistrar))
				.toList();
		}
	}

	private record ConfigCategory(
		IConfigCategory delegate,
		Consumer<Runnable> listenerRemovalRegistrar
	) implements mezz.jei.api.runtime.config.IJeiConfigCategory {
		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		@Unmodifiable
		public Collection<? extends mezz.jei.api.runtime.config.IJeiConfigValue<?>> getConfigValues() {
			return delegate.getConfigValues()
				.stream()
				.map(configValue -> ConfigManagerAdapter.createConfigValue(configValue, listenerRemovalRegistrar))
				.toList();
		}
	}

	private record ConfigValue<T>(
		IConfigValue<T> delegate,
		Consumer<Runnable> listenerRemovalRegistrar
	) implements mezz.jei.api.runtime.config.IJeiConfigValue<T> {
		private IConfigValueEditorInfo<T> getEditorInfo() {
			return delegate.getEditorInfo();
		}

		@Override
		public String getName() {
			return getEditorInfo().getName();
		}

		@Override
		public String getDescription() {
			return getLocalizedDescription().getString();
		}

		@Override
		public Component getLocalizedName() {
			return ConfigManagerAdapter.getLocalizedName(getEditorInfo().getLocalizationKey());
		}

		@Override
		public Component getLocalizedDescription() {
			return ConfigManagerAdapter.getLocalizedDescription(getEditorInfo().getLocalizationKey());
		}

		@Override
		public T getValue() {
			return delegate.get();
		}

		@Override
		public T getDefaultValue() {
			return getEditorInfo().getDefaultValue();
		}

		@Override
		public boolean set(T value) {
			return delegate.set(value);
		}

		@Override
		public void addListener(Consumer<T> listener) {
			Runnable listenerRemoval = delegate.addListener(change -> listener.accept(change.newValue()));
			listenerRemovalRegistrar.accept(listenerRemoval);
		}

		@Override
		public mezz.jei.api.runtime.config.IJeiConfigValueSerializer<T> getSerializer() {
			return createSerializer(getEditorInfo().getSerializer());
		}
	}

	private record ConfigValueSerializer<T>(
		IConfigValueSerializer<T> delegate
	) implements mezz.jei.api.runtime.config.IJeiConfigValueSerializer<T> {
		@Override
		public String serialize(T value) {
			return delegate.serialize(value);
		}

		@Override
		public IDeserializeResult<T> deserialize(String string) {
			return new DeserializeResult<>(delegate.deserialize(string));
		}

		@Override
		public boolean isValid(T value) {
			return delegate.isValid(value);
		}

		@Override
		public Optional<Collection<T>> getAllValidValues() {
			return delegate.getAllValidValues()
				.<Collection<T>>map(values -> values);
		}

		@Override
		public String getValidValuesDescription() {
			return delegate.getValidValuesDescription();
		}
	}

	private record DeserializeResult<T>(
		IDeserializeResult<T> delegate
	) implements mezz.jei.api.runtime.config.IJeiConfigValueSerializer.IDeserializeResult<T> {
		@Override
		public Optional<T> getResult() {
			return delegate.getResult();
		}

		@Override
		public List<String> getErrors() {
			return delegate.getDiagnostics();
		}
	}
}
