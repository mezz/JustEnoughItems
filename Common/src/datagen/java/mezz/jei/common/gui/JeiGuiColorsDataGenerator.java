package mezz.jei.common.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JeiGuiColorsDataGenerator {
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.create();

	private JeiGuiColorsDataGenerator() {

	}

	public static void main(String[] args) {
		Path outputPath = Path.of(args[0])
			.resolve("assets/jei/gui/colors.json");
		try {
			generate(outputPath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate JEI GUI colors", e);
		}
	}

	private static void generate(Path outputPath) throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("_comment", "JEI GUI colors. Override from a resource pack at assets/jei/gui/colors.json. Values use hex format: 0xAARRGGBB, or 0xRRGGBB for fully opaque colors. Packs may include only the colors they change.");
		for (GuiColor color : GuiColor.values()) {
			json.addProperty(color.getKey(), color.getDefaultColorString());
		}

		Files.createDirectories(outputPath.getParent());
		try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
			GSON.toJson(json, writer);
		}
	}
}
