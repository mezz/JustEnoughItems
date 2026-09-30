package mezz.jei.common.gui;

import com.google.gson.JsonParser;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JeiGuiColorsTest {
	@BeforeEach
	public void setupTheme(@TempDir Path tempDir) {
		var watcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		var manager = new ConfigManager("JEI Theme Test", watcher, watcher);
		var builder = new ConfigSchemaBuilder("jei", tempDir.resolve("theme.ini"), "jei.config.client", manager);
		var value = builder.addCategory("lists").addBoolean("darkModeEnabled", false).build();
		builder.build();
		JeiTheme.setConfigValue(value);
	}

	@AfterEach
	public void resetGuiColors() {
		JeiTheme.setDarkModeEnabled(false);
		try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of())) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}
	}

	@Test
	public void switchesLoadedPalettesWithoutReadingResourcesAgain(@TempDir Path tempDir) throws IOException {
		Path pack = createResourcePack(tempDir, "{\"recipeTextWidgetText\":\"0x112233\",\"pageNavigationText\":\"0x445566\"}");
		Path darkColors = pack.resolve("assets/jei/gui/dark/colors.json");
		Files.createDirectories(darkColors.getParent());
		Files.writeString(darkColors, "{\"recipeTextWidgetText\":\"0xAABBCC\"}");
		try (CloseableResourceManager resourceManager = createResourceManager(pack)) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}
		Files.delete(darkColors);
		Files.delete(pack.resolve("assets/jei/gui/colors.json"));

		for (boolean dark : List.of(false, true, true, false)) {
			JeiTheme.setDarkModeEnabled(dark);
			int expected = 0xFF112233;
			if (dark) {
				expected = 0xFFAABBCC;
			}
			Assertions.assertEquals(expected, JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
			Assertions.assertEquals(0xFF445566, JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));
		}
	}

	@Test
	public void reloadsBothPalettesWhileDarkModeIsSelected(@TempDir Path tempDir) throws IOException {
		Path pack = createResourcePack(tempDir, "{\"recipeTextWidgetText\":\"0x112233\"}");
		Path darkColors = pack.resolve("assets/jei/gui/dark/colors.json");
		Files.createDirectories(darkColors.getParent());
		Files.writeString(darkColors, "{\"recipeTextWidgetText\":\"0xAABBCC\"}");
		JeiTheme.setDarkModeEnabled(true);
		try (CloseableResourceManager resourceManager = createResourceManager(pack)) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
			Assertions.assertEquals(0xFFAABBCC, JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
			Files.writeString(darkColors, "{\"recipeTextWidgetText\":\"0xDDEEFF\"}");
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}
		Assertions.assertEquals(0xFFDDEEFF, JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
		JeiTheme.setDarkModeEnabled(false);
		Assertions.assertEquals(0xFF112233, JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
	}

	@Test
	public void loadColorsFromResourcePack(@TempDir Path tempDir) throws IOException {
		Path resourcePack = createResourcePack(tempDir, """
			{
			"recipeTextWidgetText": "0x112233",
			"pageNavigationBackground": "0x80224466"
			}
			""");
		try (CloseableResourceManager resourceManager = createResourceManager(resourcePack)) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}

		Assertions.assertEquals(0xFF112233, JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
		Assertions.assertEquals(0x80224466, JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND));
		Assertions.assertEquals(GuiColor.ANVIL_EXPERIENCE_COST_ERROR_TEXT.getDefaultColor(), JeiGuiColors.getColor(GuiColor.ANVIL_EXPERIENCE_COST_ERROR_TEXT));
	}

	@Test
	public void parseArgbColorString() {
		Assertions.assertEquals(0xFF808080, JeiGuiColors.parseColorString("0xFF808080").orElseThrow());
		Assertions.assertEquals(0xDDFF0000, JeiGuiColors.parseColorString("0xDDFF0000").orElseThrow());
		Assertions.assertEquals(0x30000000, JeiGuiColors.parseColorString("0x30000000").orElseThrow());
	}

	@Test
	public void parseRgbColorStringAsOpaqueArgb() {
		Assertions.assertEquals(0xFF808080, JeiGuiColors.parseColorString("0x808080").orElseThrow());
		Assertions.assertEquals(0xFFFFFFFF, JeiGuiColors.parseColorString("0xFFFFFF").orElseThrow());
	}

	@Test
	public void rejectInvalidColor() {
		Assertions.assertTrue(JeiGuiColors.parseColor(JsonParser.parseString("805306368")).isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColorString("#123456").isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColorString("123456").isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColorString("0x12345").isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColorString("0xGG000000").isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColorString("0x123456789").isEmpty());
		Assertions.assertTrue(JeiGuiColors.parseColor(JsonParser.parseString("true")).isEmpty());
	}

	private static Path createResourcePack(Path tempDir, CharSequence overrides) throws IOException {
		Path resourcePack = tempDir.resolve("jei-color-overrides");
		Files.createDirectories(resourcePack.resolve("assets/jei/gui"));
		var packFormat = DetectedVersion.BUILT_IN
			.packVersion(PackType.CLIENT_RESOURCES);
		Files.writeString(resourcePack.resolve("pack.mcmeta"), """
			{
			"pack": {
				"pack_format": {
				"major": %d,
				"minor": %d
				},
				"description": "JEI GUI color override test"
			}
			}
			""".formatted(
			packFormat.major(),
			packFormat.minor()
		));
		Files.writeString(resourcePack.resolve("assets/jei/gui/colors.json"), overrides);
		return resourcePack;
	}

	private static CloseableResourceManager createResourceManager(Path resourcePack) {
		PackLocationInfo locationInfo = new PackLocationInfo(
			"jei-color-overrides",
			Component.literal("JEI GUI color overrides"),
			PackSource.DEFAULT,
			Optional.empty()
		);
		PackResources packResources = new PathPackResources(locationInfo, resourcePack);
		return new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(packResources));
	}
}
