package mezz.jei.color;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableMap;

public class ColorNamer {
	private final ImmutableMap<Integer, String> colorNames;

	public ColorNamer(ImmutableMap<Integer, String> colorNames) {
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
