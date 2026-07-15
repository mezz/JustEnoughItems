package mezz.jei.test.lib;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TestJeiConfigValue<T> implements IJeiConfigValue<T> {
	private final String name;
	private final T defaultValue;
	private final IJeiConfigValueSerializer<T> serializer = new TestSerializer<>();
	private final List<Consumer<T>> listeners = new ArrayList<>();
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
	@SuppressWarnings("removal")
	public String getDescription() {
		return "";
	}

	@Override
	public Component getLocalizedName() {
		return Component.literal(name);
	}

	@Override
	public Component getLocalizedDescription() {
		return Component.empty();
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean set(T value) {
		this.value = value;
		listeners.forEach(listener -> listener.accept(value));
		return true;
	}

	@Override
	public void addListener(Consumer<T> listener) {
		listeners.add(listener);
	}

	@Override
	public IJeiConfigValueSerializer<T> getSerializer() {
		return serializer;
	}

	private static class TestSerializer<T> implements IJeiConfigValueSerializer<T> {
		@Override
		public String serialize(T value) {
			return String.valueOf(value);
		}

		@Override
		public IDeserializeResult<T> deserialize(String string) {
			return new IDeserializeResult<>() {
				@Override
				public Optional<T> getResult() {
					return Optional.empty();
				}

				@Override
				public List<String> getErrors() {
					return List.of("Unsupported in tests");
				}
			};
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
		public Optional<Collection<T>> getAllValidValues() {
			return Optional.empty();
		}
	}
}
