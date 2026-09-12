package mezz.jei.test;

import mezz.jei.library.color.ColorName;
import mezz.jei.library.config.serializers.ColorNameSerializer;
import net.mezzdev.config.api.value.color.ConfigColorFormat;
import net.mezzdev.config.api.value.serializer.IConfigKeyValueSerializer;
import net.mezzdev.config.api.value.color.PackedColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorNameSerializerTest {
	@Test
	public void exposesNameAndColorComponents() {
		IConfigKeyValueSerializer<ColorName, String, PackedColor> serializer = ColorNameSerializer.INSTANCE;
		ColorName lightBlue = new ColorName("LightBlue", 0x7492CC);

		Assertions.assertEquals("LightBlue:7492CC", serializer.serialize(lightBlue));
		Assertions.assertEquals("LightBlue", serializer.getKey(lightBlue));
		Assertions.assertEquals(PackedColor.rgb(0x7492CC), serializer.getValue(lightBlue));
		Assertions.assertEquals(
			new ColorName("Blue", 0x2222DD),
			serializer.createEntry("Blue", PackedColor.rgb(0x2222DD))
		);
	}

	@Test
	public void componentSerializersSupportNameAndRgbEditors() {
		ColorNameSerializer serializer = ColorNameSerializer.INSTANCE;

		String name = serializer.getKeySerializer()
			.deserialize("NewColor")
			.getResult()
			.orElseThrow();
		PackedColor color = serializer.getValueSerializer()
			.deserialize("0x123456")
			.getResult()
			.orElseThrow();

		Assertions.assertEquals("NewColor", name);
		Assertions.assertEquals(PackedColor.rgb(0x123456), color);
		Assertions.assertEquals(ConfigColorFormat.RGB, color.format());
		Assertions.assertFalse(serializer.getValueSerializer().isValid(PackedColor.argb(0xFF123456)));
	}

	@Test
	public void preservesExistingNamedColorStorageFormat() {
		ColorNameSerializer serializer = ColorNameSerializer.INSTANCE;

		ColorName color = serializer.deserialize("Custom:ABC")
			.getResult()
			.orElseThrow();

		Assertions.assertEquals(new ColorName("Custom", 0x000ABC), color);
		Assertions.assertEquals("Custom:000ABC", serializer.serialize(color));
	}

	@Test
	public void createsNewNamedColorsFromANameOnly() {
		ColorNameSerializer serializer = ColorNameSerializer.INSTANCE;

		ColorName color = serializer.deserialize("NewColor")
			.getResult()
			.orElseThrow();

		Assertions.assertEquals(new ColorName("NewColor", 0xFFFFFF), color);
		Assertions.assertEquals("A non-blank color name", serializer.getValidValuesDescription());
	}

	@Test
	public void rejectsInvalidNamesAndNonRgbColors() {
		ColorNameSerializer serializer = ColorNameSerializer.INSTANCE;

		Assertions.assertFalse(serializer.getKeySerializer().deserialize(" ").getDiagnostics().isEmpty());
		Assertions.assertFalse(serializer.getKeySerializer().deserialize("bad:name").getDiagnostics().isEmpty());
		Assertions.assertFalse(serializer.getKeySerializer().deserialize("bad,name").getDiagnostics().isEmpty());
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> serializer.createEntry("", PackedColor.rgb(0x123456))
		);
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> serializer.createEntry("Alpha", PackedColor.argb(0xFF123456))
		);
	}
}
