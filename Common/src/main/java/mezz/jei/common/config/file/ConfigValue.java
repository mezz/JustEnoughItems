package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigValue<T> implements IJeiConfigValue<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final String name;
	private final String description;
	private final T defaultValue;
	private final IJeiConfigValueSerializer<T> serializer;
	private volatile T currentValue;
	@Nullable
	private IConfigSchema schema;

	public ConfigValue(String name, T defaultValue, IJeiConfigValueSerializer<T> serializer, String description) {
		this.name = name;
		this.description = description;
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
	public String getDescription() {
		return description;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	/*
	 * Allows retreiving the default value without having to know the real data type of the value.
	 */
	@Override
	public String getSerializedDefaultValue() {
		return serializer.serialize(defaultValue);
	}

	@Override
	public T getValue() {
		if (schema != null) {
			schema.loadIfNeeded();
		}
		return currentValue;
	}

	/*
	 * Allows retreiving the value without having to know the real data type of the value.
	 */
	@Override
	public String getSerializedValue() {
		if (schema != null) {
			schema.loadIfNeeded();
		}
		return serializer.serialize(currentValue);
	}

	@Override
	public IJeiConfigValueSerializer<T> getSerializer() {
		return serializer;
	}

	/*
	 * This one is for internal loading.
	 */
	public List<String> setFromSerializedValue(String value) {
		IJeiConfigValueSerializer.IDeserializeResult<T> deserializeResult = serializer.deserialize(value);
		deserializeResult.getResult()
			.ifPresent(t -> currentValue = t);
		return deserializeResult.getErrors();
	}

	/*
	 * Update the value without knowing the exact underlying type.
	 */
	@Override
	public boolean setUsingSerializedValue(String value) {
		IJeiConfigValueSerializer.IDeserializeResult<T> deserializeResult = serializer.deserialize(value);
		if (!deserializeResult.getErrors().isEmpty()) {
			LOGGER.error("Tried to set invalid value : {}\n{}", value,  serializer.getValidValuesDescription());
			return false;
		}
		if (deserializeResult.getResult().isPresent()) {
			T realValue = deserializeResult.getResult().get();
			if (!currentValue.equals(realValue)) {
				currentValue = realValue;
				if (schema != null) {
					schema.markDirty();
				}
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean set(T value) {
		if (!serializer.isValid(value)) {
			LOGGER.error("Tried to set invalid value : {}\n{}", value,  serializer.getValidValuesDescription());
			return false;
		}
		if (!currentValue.equals(value)) {
			currentValue = value;
			if (schema != null) {
				schema.markDirty();
			}
			return true;
		}
		return false;
	}
}
