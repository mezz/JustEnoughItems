package mezz.jei.common.config.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class JsonArrayFileHelper {
	private JsonArrayFileHelper() {}

	/**
	 * Writes lines to a json array in a file, with newlines for each element.
	 */
	public static <T> void write(
		BufferedWriter out,
		int version,
		Collection<T> elements,
		Codec<T> codec,
		DynamicOps<JsonElement> registryOps,
		Consumer<? super DataResult.Error<JsonElement>> ifElementError,
		BiConsumer<T, RuntimeException> ifElementException
	) throws IOException {
		JsonArrayWriter writer = JsonArrayWriter.start(out);

		JsonObject versionElement = new JsonObject();
		versionElement.addProperty("version", version);
		writer.add(versionElement);

		for (T element : elements) {
			try {
				DataResult<JsonElement> dataResult = codec.encodeStart(registryOps, element);
				dataResult.ifError(ifElementError);
				Optional<JsonElement> resultOpt = dataResult.result();
				if (resultOpt.isPresent()) {
					JsonElement jsonElement = resultOpt.get();
					writer.add(jsonElement);
				}
			} catch (RuntimeException e) {
				ifElementException.accept(element, e);
			}
		}

		writer.end();
	}

	@Nullable
	private static Integer getVersion(JsonElement firstElement) {
		if (!firstElement.isJsonObject()) {
			return null;
		}
		JsonElement versionElement = firstElement.getAsJsonObject().get("version");
		if (versionElement.isJsonPrimitive()) {
			try {
				return versionElement.getAsInt();
			} catch (NumberFormatException | UnsupportedOperationException e) {
				return null;
			}
		}
		return null;
	}

	public static <T> List<T> read(
		BufferedReader reader,
		@Nullable Integer version,
		Codec<T> codec,
		DynamicOps<JsonElement> registryOps,
		BiConsumer<JsonElement, ? super DataResult.Error<Pair<T, JsonElement>>> ifElementError,
		BiConsumer<JsonElement, RuntimeException> ifElementException
	) throws JsonIOException, JsonSyntaxException {
		List<T> results = new ArrayList<>();
		JsonElement jsonElement = JsonParser.parseReader(reader);
		if (!jsonElement.isJsonArray()) {
			throw new JsonSyntaxException("Expected an array but got :" + jsonElement);
		}
		boolean versionFound = version == null;

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		for (JsonElement element : jsonArray) {
			if (!versionFound) {
				Integer foundVersion = getVersion(element);
				if (!version.equals(foundVersion)) {
					return List.of();
				}
				versionFound = true;
				continue;
			}
			try {
				DataResult<Pair<T, JsonElement>> dataResult = codec.decode(registryOps, element);
				dataResult.ifError(error -> {
					ifElementError.accept(element, error);
				});
				Optional<Pair<T, JsonElement>> resultOpt = dataResult.result();
				if (resultOpt.isPresent()) {
					T value = resultOpt.get().getFirst();
					results.add(value);
				}
			} catch (RuntimeException e) {
				ifElementException.accept(element, e);
			}
		}
		return results;
	}
}
