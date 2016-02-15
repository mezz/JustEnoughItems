package mezz.jei.util.color;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

public class ColorNamer {
	@Nonnull
	private final ImmutableMap<Color, String> colorNames;

	public ColorNamer(@Nonnull ImmutableMap<Color, String> colorNames) {
		this.colorNames = colorNames;
	}

	@Nonnull
	public Collection<String> getColorNames(@Nonnull ItemStack itemStack) {
		List<Color> colors = ColorGetter.getColors(itemStack, 2);
		return getColorNames(colors);
	}

	@Nonnull
	private Collection<String> getColorNames(@Nonnull List<Color> colors) {
		final Set<String> allColorNames = new LinkedHashSet<>();
		for (Color color : colors) {
			final String colorName = getClosestColorName(color);
			if (colorName != null) {
				allColorNames.add(colorName);
			}
		}
		return allColorNames;
	}

	@Nullable
	private String getClosestColorName(@Nonnull Color color) {
		if (colorNames.isEmpty()) {
			return null;
		}

		String closestColorName = null;
		Double closestColorDistance = Double.MAX_VALUE;

		for (Map.Entry<Color, String> entry : colorNames.entrySet()) {
			final Color namedColor = entry.getKey();
			final Double distance = ColorUtil.slowPerceptualColorDistanceSquared(namedColor, color);
			if (distance < closestColorDistance) {
				closestColorDistance = distance;
				closestColorName = entry.getValue();
			}
		}

		return closestColorName;
	}
}
