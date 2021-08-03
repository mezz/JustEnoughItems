package mezz.jei.config.sorting.serializers;

import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public final class SortingSerializers {
	public static final ISortingSerializer<String> STRING = new ISortingSerializer<>() {
		@Override
		public List<String> read(Reader reader) throws IOException {
			return IOUtils.readLines(reader);
		}

		@Override
		public void write(FileWriter writer, List<String> sorted) throws IOException {
			IOUtils.writeLines(sorted, "\n", writer);
		}
	};

	private SortingSerializers() {

	}
}
