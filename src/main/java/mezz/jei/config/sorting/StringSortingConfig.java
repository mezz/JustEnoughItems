package mezz.jei.config.sorting;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public abstract class StringSortingConfig extends SortingConfig<String> {
	public StringSortingConfig(File file) {
		super(file);
	}

	@Override
	protected void write(FileWriter writer, List<String> sorted) throws IOException {
		IOUtils.writeLines(sorted, "\n", writer);
	}

	@Override
	protected List<String> read(Reader reader) throws IOException {
		return IOUtils.readLines(reader);
	}
}
