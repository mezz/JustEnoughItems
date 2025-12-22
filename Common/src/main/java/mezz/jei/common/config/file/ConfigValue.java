package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigValue<T> implements IJeiConfigValue<T>, Supplier<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final String name;
	private final Component localizedName;
	private final Component description;
	private final T defaultValue;
	private final IJeiConfigValueSerializer<T> serializer;
	private @Nullable List<IConfigListener<T>> listeners;
	private volatile T currentValue;
	@Nullable
	private IConfigSchema schema;

	public ConfigValue(String localizationPath, String name, T defaultValue, IJeiConfigValueSerializer<T> serializer) {
		this.name = name;

		String nameKey = localizationPath + "." + name;
		String descriptionKey = nameKey + ".description";
		this.localizedName = Component.translatable(nameKey);
		this.description = Component.translatable(descriptionKey);
		this.defaultValue = defaultValue;
		this.currentValue = defaultValue;
		this.serializer = serializer;
	}

	public void setSchema(IConfigSchema schema) {
		this.schema = schema;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Component getLocalizedDescription() {
		return description;
	}

	@Override
	public Component getLocalizedName() {
		return localizedName;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public T getValue() {
		if (schema != null) {
			schema.loadIfNeeded();
		}
		return currentValue;
	}

	@Override
	public T get() {
		return getValue();
	}

	@Override
	public IJeiConfigValueSerializer<T> getSerializer() {
		return serializer;
	}

	public List<String> setFromSerializedValue(String value) {
		IJeiConfigValueSerializer.IDeserializeResult<T> deserializeResult = serializer.deserialize(value);
		deserializeResult.getResult()
			.ifPresent(t -> {
				if (currentValue != t) {
					currentValue = t;
					if (listeners != null) {
						listeners.forEach(c -> c.onConfigValueChanged(currentValue));
					}
				}
			});
		return deserializeResult.getErrors();
	}

	@Override
	public boolean set(T value) {
		if (!serializer.isValid(value)) {
			LOGGER.error("Tried to set invalid value : {}\n{}", value,  serializer.getValidValuesDescription());
			return false;
		}
		if (!currentValue.equals(value)) {
			currentValue = value;
			if (listeners != null) {
				listeners.forEach(c -> c.onConfigValueChanged(currentValue));
			}
			if (schema != null) {
				schema.markDirty();
			}
			return true;
		}
		return false;
	}

	public void addListener(IConfigListener<T> listener) {
		if (this.listeners == null) {
			this.listeners = new ArrayList<>();
		}
		this.listeners.add(listener);
	}

	public void clearListeners() {
		if (this.listeners != null) {
			this.listeners = null;
		}
	}
}
