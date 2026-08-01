package mezz.jei.common.gui;

import com.google.gson.JsonObject;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Path;

public final class JeiGuiColorsDataGenerator {
	private JeiGuiColorsDataGenerator() {

	}

	public static void main(String[] args) {
		Path outputPath = Path.of(args[0]);
		DataProvider provider = new JeiGuiColorsProvider(outputPath.resolve("assets/jei/gui/colors.json"));
		try {
			provider.run(CachedOutput.NO_CACHE);
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate JEI GUI colors", e);
		}
	}

	private record JeiGuiColorsProvider(Path outputPath) implements DataProvider {
		@Override
		public void run(CachedOutput cache) throws IOException {
			JsonObject json = new JsonObject();
			json.addProperty("_comment", "JEI GUI colors. Override from a resource pack at assets/jei/gui/colors.json. Values use hex format: 0xAARRGGBB, or 0xRRGGBB for fully opaque colors. Packs may include only the colors they change.");
			for (GuiColor color : GuiColor.values()) {
				json.addProperty(color.getKey(), color.getDefaultColorString());
			}
			DataProvider.saveStable(cache, json, outputPath);
		}

		@Override
		public String getName() {
			return "JEI GUI Colors";
		}
	}
}
