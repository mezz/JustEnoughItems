package mezz.jei.test.lib;

import net.mezzdev.config.api.schema.category.IConfigEditorCategory;
import net.mezzdev.config.api.value.editor.ConfigValueEditMode;
import net.mezzdev.config.api.value.editor.IConfigValueEditorInfo;
import net.mezzdev.config.api.value.editor.ConfigValueRestartRequirement;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.change.IAppliedConfigValueChange;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.change.IConfigValueBatchChangeListener;
import net.mezzdev.config.api.value.change.IConfigValueChangeListener;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestJeiConfigValue<T> implements IConfigValue<T>, IConfigValueEditorInfo<T> {
	private final String name;
	private final T defaultValue;
	private final IConfigValueSerializer<T> serializer = new TestSerializer<>();
	private final List<IConfigValueChangeListener<T>> changeListeners = new ArrayList<>();
	private T value;

	public TestJeiConfigValue(String name, T value) {
		this.name = name;
		this.defaultValue = value;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLocalizationKey() {
		return "test.config." + name;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public IConfigValueEditorInfo<T> getEditorInfo() {
		return this;
	}

	@Override
	public T getPendingValue() {
		return value;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public ConfigValueEditMode getEditMode() {
		return ConfigValueEditMode.IMMEDIATE;
	}

	@Override
	public ConfigValueRestartRequirement getRestartRequirement() {
		return ConfigValueRestartRequirement.NONE;
	}

	@Override
	public List<? extends IConfigEditorCategory> getEditorCategories() {
		return List.of();
	}

	@Override
	public boolean set(T value) {
		T oldValue = this.value;
		this.value = value;
		IAppliedConfigValueChange<T> change = new TestAppliedConfigValueChange<>(this, oldValue, value);
		changeListeners.forEach(listener -> listener.onConfigValueChanged(change));
		return true;
	}

	@Override
	public Runnable addListener(IConfigValueChangeListener<T> listener) {
		changeListeners.add(listener);
		return () -> changeListeners.remove(listener);
	}

	@Override
	public Runnable addPendingListener(IConfigValueChangeListener<T> listener) {
		return () -> {};
	}

	@Override
	public Runnable addBatchListener(IConfigValueBatchChangeListener listener) {
		return () -> {};
	}

	@Override
	public Runnable addPendingBatchListener(IConfigValueBatchChangeListener listener) {
		return () -> {};
	}

	@Override
	public IConfigValueSerializer<T> getSerializer() {
		return serializer;
	}

	private record TestAppliedConfigValueChange<T>(
		IConfigValue<T> configValue,
		T oldValue,
		T newValue
	) implements IAppliedConfigValueChange<T> {}

	private static class TestSerializer<T> implements IConfigValueSerializer<T> {
		@Override
		public String serialize(T value) {
			return String.valueOf(value);
		}

		@Override
		public IDeserializeResult<T> deserialize(String string) {
			return IDeserializeResult.failure("Unsupported in tests");
		}

		@Override
		public String getValidValuesDescription() {
			return "";
		}

		@Override
		public boolean isValid(T value) {
			return true;
		}

		@Override
		public Optional<List<T>> getAllValidValues() {
			return Optional.empty();
		}
	}
}
