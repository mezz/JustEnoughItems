package mezz.jei.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TagUtil {
	public static <VALUE, STACK> Optional<ResourceLocation> getTagEquivalent(
		Collection<STACK> stacks,
		Function<STACK, VALUE> stackToValue,
		TagCollection<VALUE> tagCollection
	) {
		if (stacks.size() < 2) {
			return Optional.empty();
		}

		List<VALUE> values = stacks.stream()
			.map(stackToValue)
			.toList();

		return tagCollection
			.getAllTags()
			.entrySet()
			.stream()
			.filter(e -> {
				Tag<VALUE> valueTags = e.getValue();
				List<VALUE> tagValues = valueTags.getValues();
				return tagValues.equals(values);
			})
			.map(Map.Entry::getKey)
			.findFirst();
	}
}
