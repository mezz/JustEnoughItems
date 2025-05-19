package mezz.jei.common.util;

import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TagUtil {
	public static <VALUE, STACK> Optional<TagKey<?>> getTagEquivalent(
		Collection<STACK> stacks,
		Function<STACK, VALUE> stackToValue,
		Supplier<Stream<HolderSet.Named<VALUE>>> tagSupplier
	) {
		List<VALUE> values = stacks.stream()
			.map(stackToValue)
			.toList();

		return tagSupplier.get()
			.filter(tag -> areEquivalent(tag, values))
			.<TagKey<?>>map(HolderSet.Named::key)
			.findFirst();
	}

	private static <VALUE> boolean areEquivalent(HolderSet.Named<VALUE> tag, List<VALUE> values) {
		int count = tag.size();
		if (count != values.size()) {
			return false;
		}
		for (int i = 0; i < count; i++) {
			VALUE tagValue = tag.get(i).value();
			VALUE value = values.get(i);
			if (!value.equals(tagValue)) {
				return false;
			}
		}
		return true;
	}
}
