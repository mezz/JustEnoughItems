package mezz.jei.config.sorting;

import mezz.jei.config.sorting.serializers.ISortingSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class SortingConfig<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File file;
	private final ISortingSerializer<T> serializer;
	@Nullable
	private List<T> sorted;

	public SortingConfig(File file, ISortingSerializer<T> serializer) {
		this.file = file;
		this.serializer = serializer;
	}

	abstract protected Comparator<T> getDefaultSortOrder();

	private void save(List<T> sorted) {
		try (FileWriter writer = new FileWriter(this.file)) {
			this.serializer.write(writer, sorted);
		} catch (IOException e) {
			LOGGER.error("Failed to save to file {}", this.file, e);
		}
	}

	@Nullable
	private List<T> loadSortedFromFile() {
		final File file = this.file;
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				return this.serializer.read(reader);
			} catch (IOException e) {
				LOGGER.error("Failed to load from file {}", file, e);
			}
		}
		return null;
	}

	private void load(Collection<T> allValues) {
		final List<T> sortedOnFile = loadSortedFromFile();
		final Comparator<T> sortOrder;
		if (sortedOnFile == null) {
			sortOrder = getDefaultSortOrder();
		} else {
			Comparator<T> existingOrder = Comparator.comparingInt(t -> indexOfSort(sortedOnFile.indexOf(t)));
			Comparator<T> defaultOrder = getDefaultSortOrder();
			sortOrder = existingOrder.thenComparing(defaultOrder);
		}

		this.sorted = allValues.stream()
			.distinct()
			.sorted(sortOrder)
			.toList();

		if (!Objects.equals(sortedOnFile, this.sorted)) {
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
