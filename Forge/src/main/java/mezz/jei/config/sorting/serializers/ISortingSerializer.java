package mezz.jei.config.sorting.serializers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface ISortingSerializer<T> {
	List<T> read(Reader reader) throws IOException;

	void write(FileWriter writer, List<T> sorted) throws IOException;
}
