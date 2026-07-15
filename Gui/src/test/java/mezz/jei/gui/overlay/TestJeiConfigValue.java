package mezz.jei.gui.overlay;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.ConfigValue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TestJeiConfigValue<T> extends ConfigValue<T> {
	public TestJeiConfigValue(String name, T value) {
		super("test", name, value, new IJeiConfigValueSerializer<>() {
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
		});
	}
}
