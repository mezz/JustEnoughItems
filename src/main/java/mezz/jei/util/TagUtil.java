package mezz.jei.util;

import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.tags.TagKey;

public class TagUtil {

	public static <TYPE> Collection<ResourceLocation> getTags(Holder.Reference<TYPE> reference) {
		return getTags(reference.tags());
	}

	public static <TYPE> Collection<ResourceLocation> getTags(Stream<TagKey<TYPE>> tags) {
		return tags.map(TagKey::location)
			.collect(Collectors.toUnmodifiableSet());
	}

	public static <VALUE, STACK> Optional<ResourceLocation> getTagEquivalent(
		Collection<STACK> stacks,
		Function<STACK, VALUE> stackToValue,
		Supplier<Stream<Pair<TagKey<VALUE>, HolderSet.Named<VALUE>>>> tagSupplier
	) {
		if (stacks.size() < 2) {
			return Optional.empty();
		}

		List<VALUE> values = stacks.stream()
			.map(stackToValue)
			.toList();

		return tagSupplier.get()
			.filter(e -> {
				HolderSet.Named<VALUE> tag = e.getSecond();
				int count = tag.size();
				if (count == values.size()) {
					return IntStream.range(0, count).allMatch(i -> {
						VALUE tagValue = tag.get(i).value();
						VALUE value = values.get(i);
						return value.equals(tagValue);
					});
				}
				return false;
			})
			.map(e -> e.getFirst().location())
			.findFirst();
	}
}
