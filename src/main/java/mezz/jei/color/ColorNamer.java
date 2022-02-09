package mezz.jei.color;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import mezz.jei.util.Translator;

public class ColorNamer {
	private final ImmutableMap<Integer, String> colorNames;

	public ColorNamer(ImmutableMap<Integer, String> colorNames) {
		this.colorNames = colorNames;
	}

	public Collection<String> getColorNames(Iterable<Integer> colors, boolean lowercase) {
		final Set<String> allColorNames = new LinkedHashSet<>();
		for (Integer color : colors) {
			final String colorName = getClosestColorName(color);
			if (colorName != null) {
				if (lowercase) {
					allColorNames.add(Translator.toLowercaseWithLocale(colorName));
				} else {
					allColorNames.add(colorName);
				}
			}
		}
		return allColorNames;
	}

	@Nullable
	private String getClosestColorName(Integer color) {
		if (colorNames.isEmpty()) {
			return null;
		}

		String closestColorName = null;
		double closestColorDistance = Double.MAX_VALUE;

		for (Map.Entry<Integer, String> entry : colorNames.entrySet()) {
			final Integer namedColor = entry.getKey();
			final double distance = ColorUtil.slowPerceptualColorDistanceSquared(namedColor, color);
			final double absDistance = Math.abs(distance);
			if (absDistance < closestColorDistance) {
				closestColorDistance = absDistance;
				closestColorName = entry.getValue();
			}
		}

		return closestColorName;
	}
}
