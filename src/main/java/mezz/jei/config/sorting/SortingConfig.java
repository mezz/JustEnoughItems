package mezz.jei.config.sorting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public abstract class SortingConfig<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File file;
	@Nullable
	private List<T> sorted;

	public SortingConfig(File file) {
		this.file = file;
	}

	abstract protected List<T> read(Reader reader) throws IOException;
	abstract protected void write(FileWriter writer, List<T> sorted) throws IOException;

	abstract protected Comparator<T> getDefaultSortOrder();

	private void save(List<T> sorted) {
		try (FileWriter writer = new FileWriter(this.file)) {
			write(writer, sorted);
		} catch (IOException e) {
			LOGGER.error("Failed to save to file {}", this.file, e);
		}
	}

	@Nullable
	private List<T> loadSortedFromFile() {
		final File file = this.file;
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				return read(reader);
			} catch (IOException e) {
				LOGGER.error("Failed to load from file {}", file, e);
			}
		}
		return null;
	}

	private void load(Set<T> allValues) {
		final List<T> sortedOnFile = loadSortedFromFile();
		final Comparator<T> sortOrder;
		if (sortedOnFile == null) {
			sortOrder = getDefaultSortOrder();
		} else {
			Comparator<T> existingOrder = Comparator.comparingInt(t -> fileSortIndex(sortedOnFile::indexOf, t));
			Comparator<T> defaultOrder = getDefaultSortOrder();
			sortOrder = existingOrder.thenComparing(defaultOrder);
		}

		this.sorted = allValues.stream()
			.sorted(sortOrder)
			.collect(Collectors.toList());

		if (!Objects.equals(sortedOnFile, this.sorted)) {
			save(this.sorted);
		}
	}

	private int fileSortIndex(ToIntFunction<T> indexOfFunc, T value) {
		int index = indexOfFunc.applyAsInt(value);
		if (index < 0) {
			index = Integer.MAX_VALUE;
		}
		return index;
	}

	public List<T> getSorted(Set<T> allValues) {
		if (this.sorted == null) {
			load(allValues);
		}
		return Collections.unmodifiableList(this.sorted);
	}

}
