package mezz.jei.common.config.sorting.serializers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ISortingSerializer<T> {
	List<T> read(Path path) throws IOException;

	void write(Path path, List<T> sorted) throws IOException;
}
