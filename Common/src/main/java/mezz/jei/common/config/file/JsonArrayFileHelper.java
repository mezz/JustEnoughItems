package mezz.jei.common.config.file;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

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
		Collection<T> elements,
		Codec<T> codec,
		DynamicOps<JsonElement> registryOps,
		Consumer<? super DataResult.Error<JsonElement>> ifError
	) throws IOException {
		Gson gson = new Gson();
		JsonArrayWriter writer = JsonArrayWriter.start(out);

		for (T element : elements) {
			DataResult<JsonElement> dataResult = codec.encodeStart(registryOps, element);
			dataResult.ifError(ifError);
			Optional<JsonElement> resultOpt = dataResult.result();
			if (resultOpt.isPresent()) {
				JsonElement jsonElement = resultOpt.get();
				String line = gson.toJson(jsonElement);
				writer.add(line);
			}
		}

		writer.end();
	}

	public static <T> List<T> read(
		BufferedReader reader,
		Codec<T> codec,
		DynamicOps<JsonElement> registryOps,
		BiConsumer<JsonElement, ? super DataResult.Error<Pair<T, JsonElement>>> ifError
	) throws JsonIOException, JsonSyntaxException {
		List<T> results = new ArrayList<>();
		JsonElement jsonElement = JsonParser.parseReader(reader);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		for (JsonElement element : jsonArray) {
			DataResult<Pair<T, JsonElement>> dataResult = codec.decode(registryOps, element);
			dataResult.ifError(error -> {
				ifError.accept(element, error);
			});
			Optional<Pair<T, JsonElement>> resultOpt = dataResult.result();
			if (resultOpt.isPresent()) {
				T value = resultOpt.get().getFirst();
				results.add(value);
			}
		}
		return results;
	}
}
