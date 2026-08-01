package mezz.jei.common.gui;

import com.google.gson.JsonObject;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class JeiGuiColorsDataGenerator {
	private JeiGuiColorsDataGenerator() {

	}

	public static void main(String[] args) {
		Path outputPath = Path.of(args[0]);
		PackOutput packOutput = new PackOutput(outputPath);
		DataProvider provider = new JeiGuiColorsProvider(packOutput);
		provider.run(CachedOutput.NO_CACHE)
			.join();
	}

	private record JeiGuiColorsProvider(PackOutput.PathProvider pathProvider) implements DataProvider {
		private static final ResourceLocation COLORS = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "colors");

		private JeiGuiColorsProvider(PackOutput output) {
			this(output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "gui"));
		}

		@Override
		public CompletableFuture<?> run(CachedOutput cache) {
			JsonObject json = new JsonObject();
			json.addProperty("_comment", "JEI GUI colors. Override from a resource pack at assets/jei/gui/colors.json. Values use hex format: 0xAARRGGBB, or 0xRRGGBB for fully opaque colors. Packs may include only the colors they change.");
			for (GuiColor color : GuiColor.values()) {
				json.addProperty(color.getKey(), color.getDefaultColorString());
			}
			return DataProvider.saveStable(cache, json, pathProvider.json(COLORS));
		}

		@Override
		public String getName() {
			return "JEI GUI Colors";
		}
	}
}
