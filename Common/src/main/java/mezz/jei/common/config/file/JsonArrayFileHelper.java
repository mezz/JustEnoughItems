package mezz.jei.common.config.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mezz.jei.common.util.PathUtil;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class JsonArrayFileHelper {
	private static final String VERSION_KEY = "version";

	private JsonArrayFileHelper() {

	}

	/**
	 * Writes elements to a json array in a file, with one element per line.
	 */
	public static void write(Path path, int version, Collection<JsonElement> elements) throws IOException {
		Files.createDirectories(path.getParent());
		Path tempFile = Files.createTempFile(path.getParent(), null, null);
		try {
			try (BufferedWriter out = Files.newBufferedWriter(tempFile)) {
				JsonArrayWriter writer = JsonArrayWriter.start(out);

				JsonObject versionElement = new JsonObject();
				versionElement.addProperty(VERSION_KEY, version);
				writer.add(versionElement);

				for (JsonElement element : elements) {
					writer.add(element);
				}

				writer.end();
			}
			PathUtil.moveAtomicReplace(tempFile, path);
		} finally {
			if (Files.exists(tempFile)) {
				Files.delete(tempFile);
			}
		}
	}

	public static List<JsonElement> read(
		Path path,
		int version,
		BiConsumer<JsonElement, String> ifElementError
	) throws IOException, JsonSyntaxException {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			JsonElement jsonElement = JsonParser.parseReader(reader);
			if (!jsonElement.isJsonArray()) {
				throw new JsonSyntaxException("Expected an array but got: " + jsonElement);
			}

			JsonArray jsonArray = jsonElement.getAsJsonArray();
			if (jsonArray.isEmpty()) {
				return List.of();
			}

			JsonElement firstElement = jsonArray.get(0);
			Integer foundVersion = getVersion(firstElement);
			if (!Objects.equals(version, foundVersion)) {
				ifElementError.accept(firstElement, "Expected version " + version + " but got " + foundVersion);
				return List.of();
			}

			List<JsonElement> results = new ArrayList<>();
			for (int i = 1; i < jsonArray.size(); i++) {
				results.add(jsonArray.get(i));
			}
			return results;
		}
	}

	@Nullable
	private static Integer getVersion(JsonElement firstElement) {
		if (!firstElement.isJsonObject()) {
			return null;
		}
		JsonElement versionElement = firstElement.getAsJsonObject().get(VERSION_KEY);
		if (versionElement == null || !versionElement.isJsonPrimitive()) {
			return null;
		}
		try {
			return versionElement.getAsInt();
		} catch (NumberFormatException | UnsupportedOperationException e) {
			return null;
		}
	}
}
