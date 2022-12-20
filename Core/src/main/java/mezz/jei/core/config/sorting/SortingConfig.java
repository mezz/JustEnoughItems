package mezz.jei.core.config.sorting;

import mezz.jei.core.config.sorting.serializers.ISortingSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public abstract class SortingConfig<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Path path;
	private final ISortingSerializer<T> serializer;
	@Nullable
	private List<T> sorted;

	public SortingConfig(Path path, ISortingSerializer<T> serializer) {
		this.path = path;
		this.serializer = serializer;
	}

	abstract protected Comparator<T> getDefaultSortOrder();

	private void save(List<T> sorted) {
		try {
			this.serializer.write(path, sorted);
		} catch (IOException e) {
			LOGGER.error("Failed to save to file {}", this.path, e);
		}
	}

	private Optional<List<T>> loadSortedFromFile() {
		if (Files.exists(path)) {
			try {
				List<T> result = this.serializer.read(path);
				return Optional.of(result);
			} catch (IOException e) {
				LOGGER.error("Failed to load from file: {}", path, e);
			}
		}
		return Optional.empty();
	}

	private void load(Collection<T> allValues) {
		final Optional<List<T>> previousSorted = loadSortedFromFile();

		final Comparator<T> defaultOrder = getDefaultSortOrder();
		final Comparator<T> sortOrder = previousSorted
			.map(s -> {
				Comparator<T> existingOrder = Comparator.comparingInt(t -> indexOfSort(s.indexOf(t)));
				return existingOrder.thenComparing(defaultOrder);
			})
			.orElse(defaultOrder);

		this.sorted = allValues.stream()
			.distinct()
			.sorted(sortOrder)
			.toList();

		final boolean changed = previousSorted
			.map(s -> !Objects.equals(s, this.sorted))
			.orElse(true);

		if (changed) {
			save(this.sorted);
		}
	}

	private static int indexOfSort(int index) {
		if (index < 0) {
			return Integer.MAX_VALUE;
		}
		return index;
	}

	private List<T> getSorted(Collection<T> allValues) {
		if (this.sorted == null) {
			load(allValues);
		}
		return this.sorted;
	}

	public <V> Comparator<V> getComparator(Collection<T> allValues, Function<V, T> mapping) {
		List<T> sorted = getSorted(allValues);
		return Comparator.comparingInt(o -> {
			T value = mapping.apply(o);
			return indexOfSort(sorted.indexOf(value));
		});
	}

}
