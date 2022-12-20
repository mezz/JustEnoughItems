package mezz.jei.core.config.sorting.serializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SortingSerializers {
	public static final ISortingSerializer<String> STRING = new ISortingSerializer<>() {
		@Override
		public List<String> read(Path path) throws IOException {
			return Files.readAllLines(path);
		}

		@Override
		public void write(Path path, List<String> sorted) throws IOException {
			Files.write(path, sorted);
		}
	};

	private SortingSerializers() {

	}
}
