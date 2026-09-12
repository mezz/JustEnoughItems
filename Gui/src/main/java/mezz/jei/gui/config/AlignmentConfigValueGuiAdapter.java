package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.gui.api.ConfigValueApplyMode;
import net.mezzdev.config.gui.api.ConfigValueEditorType;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IConfigValueEditorSerializer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Adapts separate horizontal and vertical alignment config values into one screen value.
 */
final class AlignmentConfigValueGuiAdapter implements IConfigScreenValue<Alignment> {
	public static final ConfigValueEditorType<Alignment> EDITOR_TYPE = ConfigValueEditorType.create(ModIds.JEI_ID, "alignment");
	private static final IConfigValueEditorSerializer<Alignment> SERIALIZER = new AlignmentSerializer();

	private final String localizationKey;
	private final IConfigValue<HorizontalAlignment> horizontalAlignment;
	private final IConfigValue<VerticalAlignment> verticalAlignment;

	public AlignmentConfigValueGuiAdapter(
		String localizationKey,
		IConfigValue<HorizontalAlignment> horizontalAlignment,
		IConfigValue<VerticalAlignment> verticalAlignment
	) {
		this.localizationKey = localizationKey;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
	}

	@Override
	public String getName() {
		return "alignment";
	}

	@Override
	public String getLocalizationKey() {
		return localizationKey;
	}

	@Override
	public Alignment getValue() {
		return Alignment.from(horizontalAlignment.get(), verticalAlignment.get());
	}

	@Override
	public Alignment getDefaultValue() {
		return Alignment.from(
			horizontalAlignment.getEditorInfo().getDefaultValue(),
			verticalAlignment.getEditorInfo().getDefaultValue()
		);
	}

	@Override
	public boolean set(Alignment value) {
		if (!SERIALIZER.isValid(value)) {
			throw new IllegalArgumentException("Invalid alignment: " + value);
		}
		boolean horizontalChanged = horizontalAlignment.set(value.horizontalAlignment());
		boolean verticalChanged = verticalAlignment.set(value.verticalAlignment());
		return horizontalChanged || verticalChanged;
	}

	@Override
	public Runnable addListener(Consumer<Alignment> listener) {
		Runnable removeHorizontalListener = horizontalAlignment.addListener(value -> listener.accept(getValue()));
		Runnable removeVerticalListener = verticalAlignment.addListener(value -> listener.accept(getValue()));
		return () -> {
			removeHorizontalListener.run();
			removeVerticalListener.run();
		};
	}

	@Override
	public ConfigValueApplyMode getApplyMode() {
		return ConfigValueApplyMode.IMMEDIATE;
	}

	@Override
	public IConfigValueEditorSerializer<Alignment> getSerializer() {
		return SERIALIZER;
	}

	private static final class AlignmentSerializer implements IConfigValueEditorSerializer<Alignment> {
		private static final List<Alignment> VALID_VALUES = List.of(Alignment.values());

		@Override
		public String serialize(Alignment value) {
			return value.name();
		}

		@Override
		public IDeserializeResult<Alignment> deserialize(String string) {
			string = string.trim();
			if (string.startsWith("\"") && string.endsWith("\"")) {
				string = string.substring(1, string.length() - 1);
			}
			try {
				return IDeserializeResult.success(Alignment.valueOf(string));
			} catch (IllegalArgumentException e) {
				return IDeserializeResult.failure("Invalid alignment name: %s".formatted(e.getMessage()));
			}
		}

		@Override
		public boolean isValid(Alignment value) {
			return VALID_VALUES.contains(value);
		}

		@Override
		public Optional<List<Alignment>> getAllValidValues() {
			return Optional.of(VALID_VALUES);
		}

		@Override
		public String getValidValuesDescription() {
			return VALID_VALUES.toString();
		}

		@Override
		public ConfigValueEditorType<Alignment> getEditorType() {
			return EDITOR_TYPE;
		}

		@Override
		public Component getLocalizedValueName(String configValueLocalizationKey, Alignment value) {
			return Component.translatable("jei.config.value.Alignment." + value.name() + ".name");
		}
	}
}
