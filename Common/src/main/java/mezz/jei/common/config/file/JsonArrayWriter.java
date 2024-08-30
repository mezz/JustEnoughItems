package mezz.jei.common.config.file;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes lines to a json array in a file, with newlines for each element.
 */
public class JsonArrayWriter {
	private final BufferedWriter out;
	private boolean firstLine = true;

	public static JsonArrayWriter start(BufferedWriter out) throws IOException {
		JsonArrayWriter writer = new JsonArrayWriter(out);
		out.write("[\n");
		return writer;
	}

	private JsonArrayWriter(BufferedWriter out) {
		this.out = out;
	}

	public void add(String line) throws IOException {
		if (firstLine) {
			out.write("  ");
			firstLine = false;
		} else {
			out.write(",\n  ");
		}
		out.write(line);
	}

	public void end() throws IOException {
		out.write("\n]");
		out.flush();
	}
}
