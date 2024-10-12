package mezz.jei.library.config;

import mezz.jei.library.color.ColorName;
import mezz.jei.library.color.ColorUtil;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.library.config.serializers.ColorNameSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public final class ColorNameConfig {
	private static final List<ColorName> defaultColors = List.of(
		new ColorName("White", 0xEEEEEE),
		new ColorName("LightBlue", 0x7492cc),
		new ColorName("Cyan", 0x00EEEE),
		new ColorName("Blue", 0x2222dd),
		new ColorName("LapisBlue", 0x25418b),
		new ColorName("Teal", 0x008080),
		new ColorName("Yellow", 0xcacb58),
		new ColorName("GoldenYellow", 0xEED700),
		new ColorName("Orange", 0xd97634),
		new ColorName("Pink", 0xD1899D),
		new ColorName("HotPink", 0xFC0FC0),
		new ColorName("Magenta", 0xb24bbb),
		new ColorName("Purple", 0x813eb9),
		new ColorName("EvilPurple", 0x2e1649),
		new ColorName("Lavender", 0xB57EDC),
		new ColorName("Indigo", 0x480082),
		new ColorName("Sand", 0xdbd3a0),
		new ColorName("Tan", 0xbb9b63),
		new ColorName("LightBrown", 0xA0522D),
		new ColorName("Brown", 0x634b33),
		new ColorName("DarkBrown", 0x3a2d13),
		new ColorName("LimeGreen", 0x43b239),
		new ColorName("SlimeGreen", 0x83cb73),
		new ColorName("Green", 0x008000),
		new ColorName("DarkGreen", 0x224d22),
		new ColorName("GrassGreen", 0x548049),
		new ColorName("Red", 0x963430),
		new ColorName("BrickRed", 0xb0604b),
		new ColorName("NetherBrick", 0x2a1516),
		new ColorName("Redstone", 0xce3e36),
		new ColorName("Black", 0x181515),
		new ColorName("CharcoalGray", 0x464646),
		new ColorName("IronGray", 0x646464),
		new ColorName("Gray", 0x808080),
		new ColorName("Silver", 0xC0C0C0)
	);

	private final Supplier<List<ColorName>> searchColors;

	public ColorNameConfig(IConfigSchemaBuilder schema) {
		IConfigCategoryBuilder colors = schema.addCategory("colors");
		this.searchColors = colors.addList(
			"SearchColors",
			defaultColors,
			new ListSerializer<>(ColorNameSerializer.INSTANCE),
			"Color values to search for."
		);
	}

	public String getClosestColorName(int color) {
		List<ColorName> colorNames = searchColors.get();
		if (colorNames.isEmpty()) {
			colorNames = defaultColors;
		}
		return colorNames
			.stream()
			.min(Comparator.comparing(entry -> {
				int namedColor = entry.color();
				double distance = ColorUtil.slowPerceptualColorDistanceSquared(namedColor, color);
				return Math.abs(distance);
			}))
			.map(ColorName::name)
			.orElseThrow();
	}
}
