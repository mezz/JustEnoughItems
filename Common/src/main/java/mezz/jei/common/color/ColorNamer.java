package mezz.jei.common.color;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableMap;

public class ColorNamer {
	@Nullable
	private static ColorNamer INSTANCE = null;

	public static ColorNamer getInstance() {
		Preconditions.checkState(INSTANCE != null, "Color Namer has not been created yet.");
		return INSTANCE;
	}

	public static void create(ImmutableMap<Integer, String> colorNames) {
		INSTANCE = new ColorNamer(colorNames);
	}

	private final ImmutableMap<Integer, String> colorNames;

	private ColorNamer(ImmutableMap<Integer, String> colorNames) {
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
		return colorNames.entrySet().stream()
			.min(Comparator.comparing(entry -> {
				Integer namedColor = entry.getKey();
				double distance = ColorUtil.slowPerceptualColorDistanceSquared(namedColor, color);
				return Math.abs(distance);
			}))
			.map(Map.Entry::getValue)
			.orElse(null);
	}
}
