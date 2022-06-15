package mezz.jei.common.color;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ColorNamer {
	@Nullable
	private static ColorNamer INSTANCE = null;

	public static ColorNamer getInstance() {
		Preconditions.checkState(INSTANCE != null, "Color Namer has not been created yet.");
		return INSTANCE;
	}

	public static void create(Supplier<List<ColorName>> colorNames) {
		INSTANCE = new ColorNamer(colorNames);
	}

	private final Supplier<List<ColorName>> colorNames;

	private ColorNamer(Supplier<List<ColorName>> colorNames) {
		this.colorNames = colorNames;
	}

	public Stream<String> getColorNames(Iterable<Integer> colors) {
		return StreamSupport.stream(colors.spliterator(), false)
			.<String>mapMulti((color, consumer) -> {
				String colorName = getClosestColorName(color);
				if (colorName != null) {
					consumer.accept(colorName);
				}
			})
			.distinct();
	}

	@Nullable
	private String getClosestColorName(Integer color) {
		return colorNames.get().stream()
			.min(Comparator.comparing(entry -> {
				int namedColor = entry.color();
				double distance = ColorUtil.slowPerceptualColorDistanceSquared(namedColor, color);
				return Math.abs(distance);
			}))
			.map(ColorName::name)
			.orElse(null);
	}
}
