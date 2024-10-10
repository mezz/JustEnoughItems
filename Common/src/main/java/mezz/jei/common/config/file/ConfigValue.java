package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ConfigValue<T> implements IJeiConfigValue<T>, Supplier<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final String name;
	private final Component localizedName;
	private final Component description;
	private final T defaultValue;
	private final IJeiConfigValueSerializer<T> serializer;
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

	@SuppressWarnings("removal")
	@Override
	public String getDescription() {
		return description.getString();
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
			.ifPresent(t -> currentValue = t);
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
			if (schema != null) {
				schema.markDirty();
			}
			return true;
		}
		return false;
	}
}
