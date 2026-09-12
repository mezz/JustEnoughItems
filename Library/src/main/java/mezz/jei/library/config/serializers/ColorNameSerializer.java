package mezz.jei.library.config.serializers;

import net.mezzdev.config.api.value.color.ConfigColorFormat;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigKeyValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.api.value.color.PackedColor;
import mezz.jei.library.color.ColorName;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColorNameSerializer implements IConfigKeyValueSerializer<ColorName, String, PackedColor> {
	public static final ColorNameSerializer INSTANCE = new ColorNameSerializer();
	private static final PackedColor DEFAULT_NEW_COLOR = PackedColor.rgb(0xFFFFFF);
	private static final IConfigValueSerializer<String> NAME_SERIALIZER = new ColorNameSerializer.NameSerializer();
	private static final IConfigValueSerializer<PackedColor> COLOR_SERIALIZER = new ColorNameSerializer.RgbColorSerializer();

	private ColorNameSerializer() {}

	@Override
	public IConfigValueSerializer<String> getKeySerializer() {
		return NAME_SERIALIZER;
	}

	@Override
	public IConfigValueSerializer<PackedColor> getValueSerializer() {
		return COLOR_SERIALIZER;
	}

	@Override
	public String getKey(ColorName entry) {
		return entry.name();
	}

	@Override
	public PackedColor getValue(ColorName entry) {
		return PackedColor.rgb(entry.color());
	}

	@Override
	public ColorName createEntry(String key, PackedColor value) {
		if (!NAME_SERIALIZER.isValid(key)) {
			throw new IllegalArgumentException("Color name entries require a valid name.");
		}
		if (!COLOR_SERIALIZER.isValid(value)) {
			throw new IllegalArgumentException("Color name entries require an RGB color.");
		}
		return new ColorName(key, value.packedValue());
	}

	@Override
	public String serialize(ColorName value) {
		return "%s:%06X".formatted(value.name(), value.color());
	}

	@Override
	public IDeserializeResult<ColorName> deserialize(String string) {
		string = string.trim();
		if (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		String[] values = string.split(":", 2);
		if (values.length == 1) {
			return NAME_SERIALIZER.deserialize(values[0])
				.getResult()
				.map(name -> IDeserializeResult.success(createEntry(name, DEFAULT_NEW_COLOR)))
				.orElseGet(() -> IDeserializeResult.failure("Color entry must have a valid name."));
		}
		if (values.length == 2) {
			IDeserializeResult<String> nameResult = NAME_SERIALIZER.deserialize(values[0]);
			IDeserializeResult<PackedColor> colorResult = COLOR_SERIALIZER.deserialize("0x" + values[1]);
			if (nameResult.getDiagnostics().isEmpty() && colorResult.getDiagnostics().isEmpty()) {
				Optional<String> name = nameResult.getResult();
				Optional<PackedColor> color = colorResult.getResult();
				if (name.isPresent() && color.isPresent()) {
					return IDeserializeResult.success(createEntry(name.get(), color.get()));
				}
			}
			List<String> diagnostics = new ArrayList<>(nameResult.getDiagnostics());
			diagnostics.addAll(colorResult.getDiagnostics());
			return IDeserializeResult.failure(diagnostics);
		}
		String errorMsg = "Color entry must be a name and an RGB hex color, separated by a ':'.";
		return IDeserializeResult.failure(errorMsg);
	}

	@Override
	public String getValidValuesDescription() {
		return NAME_SERIALIZER.getValidValuesDescription();
	}

	@Override
	public boolean isValid(@Nullable ColorName value) {
		return value != null &&
			NAME_SERIALIZER.isValid(value.name()) &&
			(value.color() & 0xFF00_0000) == 0;
	}

	private static final class NameSerializer implements IConfigValueSerializer<String> {
		@Override
		public String serialize(String value) {
			return value;
		}

		@Override
		public IDeserializeResult<String> deserialize(String string) {
			String name = string.trim();
			if (!isValid(name)) {
				return IDeserializeResult.failure("Color names must not be blank or contain ':' or ','.");
			}
			return IDeserializeResult.success(name);
		}

		@Override
		public boolean isValid(@Nullable String value) {
			return value != null &&
				!value.isBlank() &&
				!value.contains(":") &&
				!value.contains(",");
		}

		@Override
		public String getValidValuesDescription() {
			return "A non-blank color name";
		}
	}

	private static final class RgbColorSerializer implements IConfigValueSerializer<PackedColor> {
		private static final String HEX_PREFIX = "0x";
		private static final int HEX_DIGITS = 6;

		@Override
		public String serialize(PackedColor value) {
			return "0x%06X".formatted(value.packedValue());
		}

		@Override
		public IDeserializeResult<PackedColor> deserialize(String string) {
			String value = string.trim();
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}
			if (!value.regionMatches(true, 0, HEX_PREFIX, 0, HEX_PREFIX.length())) {
				return IDeserializeResult.failure("Invalid color. Must be: " + getValidValuesDescription());
			}
			String hex = value.substring(HEX_PREFIX.length());
			if (hex.isEmpty() || hex.length() > HEX_DIGITS) {
				return IDeserializeResult.failure("Invalid color. Must be: " + getValidValuesDescription());
			}
			try {
				return IDeserializeResult.success(PackedColor.rgb(Integer.parseUnsignedInt(hex, 16)));
			} catch (NumberFormatException e) {
				return IDeserializeResult.failure("Unable to parse RGB color: '" + string + "'");
			}
		}

		@Override
		public boolean isValid(@Nullable PackedColor value) {
			return value != null && value.format() == ConfigColorFormat.RGB;
		}

		@Override
		public String getValidValuesDescription() {
			return "An RGB color serialized as 0xRRGGBB";
		}
	}
}
