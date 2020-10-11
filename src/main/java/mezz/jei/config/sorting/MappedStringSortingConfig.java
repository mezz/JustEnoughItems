package mezz.jei.config.sorting;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class MappedStringSortingConfig<V> extends StringSortingConfig {
	private final Function<V, String> mapping;

	public MappedStringSortingConfig(File file, Function<V, String> mapping) {
		super(file);
		this.mapping = mapping;
	}

	public Comparator<V> getComparator(Collection<V> allValues) {
		Set<String> allMappedValues = allValues.stream()
			.map(mapping)
			.collect(Collectors.toSet());
		return super.getComparator(allMappedValues, mapping);
	}

	public Comparator<V> getComparatorFromMappedValues(Set<String> allMappedValues) {
		return super.getComparator(allMappedValues, mapping);
	}
}
