package mezz.jei.config.sorting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SortingConfig<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File file;
	private final Collection<Consumer<List<T>>> listeners = new ArrayList<>();
	@Nullable
	private List<T> sorted;

	public SortingConfig(File file) {
		this.file = file;
	}

	abstract protected List<T> read(Reader reader) throws IOException;
	abstract protected void write(FileWriter writer, List<T> sorted) throws IOException;

	abstract protected Comparator<T> getDefaultSortOrder();
	abstract protected Stream<T> generate();

	private void save() {
		List<T> sorted = getSorted();
		try (FileWriter writer = new FileWriter(this.file)) {
			write(writer, sorted);
		} catch (IOException e) {
			LOGGER.error("Failed to save to file {}", this.file, e);
		}
	}

	private void load() {
		final File file = this.file;
		List<T> sortedOnFile = null;
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				sortedOnFile = read(reader);
			} catch (IOException e) {
				LOGGER.error("Failed to load from file {}", file, e);
			}
		}

		Comparator<T> sortOrder = getDefaultSortOrder();
		if (sortedOnFile != null) {
			Comparator<T> existingOrder = Comparator.comparing(sortedOnFile::indexOf);
			sortOrder = existingOrder.thenComparing(sortOrder);
		}

		final List<T> previousSorted = this.sorted;
		this.sorted = generate()
			.sorted(sortOrder)
			.collect(Collectors.toList());

		if (!Objects.equals(previousSorted, this.sorted)) {
			List<T> unmodifiableList = Collections.unmodifiableList(this.sorted);
			for (Consumer<List<T>> listener : this.listeners) {
				listener.accept(unmodifiableList);
			}
		}
		if (!Objects.equals(sortedOnFile, this.sorted)) {
			save();
		}
	}

	public List<T> getSorted() {
		if (this.sorted == null) {
			load();
		}
		return Collections.unmodifiableList(this.sorted);
	}

	public void addListener(Consumer<List<T>> listener) {
		this.listeners.add(listener);
	}
}
