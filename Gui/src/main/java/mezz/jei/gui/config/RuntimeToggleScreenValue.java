package mezz.jei.gui.config;

import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.gui.api.ConfigValueApplyMode;
import net.mezzdev.config.gui.api.IConfigScreenValue;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

final class RuntimeToggleScreenValue implements IConfigScreenValue<Boolean> {
	private final String name;
	private final String localizationKey;
	private final boolean defaultValue;
	private final BooleanSupplier valueSupplier;
	private final Consumer<Boolean> valueSetter;
	private final Function<Consumer<Boolean>, Runnable> listenerRegistrar;
	private final IConfigValueSerializer<Boolean> serializer;

	RuntimeToggleScreenValue(
		String name,
		String localizationKey,
		boolean defaultValue,
		BooleanSupplier valueSupplier,
		Consumer<Boolean> valueSetter,
		Function<Consumer<Boolean>, Runnable> listenerRegistrar,
		IConfigValueSerializer<Boolean> serializer
	) {
		this.name = name;
		this.localizationKey = localizationKey;
		this.defaultValue = defaultValue;
		this.valueSupplier = valueSupplier;
		this.valueSetter = valueSetter;
		this.listenerRegistrar = listenerRegistrar;
		this.serializer = serializer;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLocalizationKey() {
		return localizationKey;
	}

	@Override
	public Boolean getValue() {
		return valueSupplier.getAsBoolean();
	}

	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean set(Boolean value) {
		if (value == null || !serializer.isValid(value)) {
			throw new IllegalArgumentException("Invalid value for " + name + ": " + value);
		}
		if (valueSupplier.getAsBoolean() == value) {
			return false;
		}
		valueSetter.accept(value);
		return valueSupplier.getAsBoolean() == value;
	}

	@Override
	public Runnable addListener(Consumer<Boolean> listener) {
		return listenerRegistrar.apply(listener);
	}

	@Override
	public ConfigValueApplyMode getApplyMode() {
		return ConfigValueApplyMode.IMMEDIATE;
	}

	@Override
	public IConfigValueSerializer<Boolean> getSerializer() {
		return serializer;
	}
}
