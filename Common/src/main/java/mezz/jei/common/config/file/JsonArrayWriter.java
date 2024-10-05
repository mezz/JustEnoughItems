package mezz.jei.common.config.file;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes lines to a json array in a file, with newlines for each element.
 */
public class JsonArrayWriter {
	private final Gson gson = new Gson();
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

	public void add(JsonElement line) throws IOException {
		if (firstLine) {
			out.write("  ");
			firstLine = false;
		} else {
			out.write(",\n  ");
		}
		gson.toJson(line, out);
	}

	public void end() throws IOException {
		out.write("\n]");
		out.flush();
	}
}
