package mezz.jei.core.config.sorting;

import mezz.jei.core.config.sorting.serializers.ISortingSerializer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class MappedSortingConfig<T, V> extends SortingConfig<V> {
	private final Function<T, V> mapping;

	public MappedSortingConfig(Path path, ISortingSerializer<V> serializer, Function<T, V> mapping) {
		super(path, serializer);
		this.mapping = mapping;
	}

	public Comparator<T> getComparator(Collection<T> allValues) {
		Set<V> allMappedValues = allValues.stream()
			.map(mapping)
			.collect(Collectors.toSet());
		return super.getComparator(allMappedValues, mapping);
	}

	public Comparator<T> getComparatorFromMappedValues(Collection<V> allMappedValues) {
		return super.getComparator(allMappedValues, mapping);
	}
}
