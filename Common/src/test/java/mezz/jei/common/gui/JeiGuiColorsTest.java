package mezz.jei.common.gui;

import com.google.gson.JsonParser;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.DetectedVersion;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JeiGuiColorsTest {
	@AfterEach
	public void resetGuiColors() {
		try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of())) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}
	}

	@Test
	public void loadColorsFromResourcePack(@TempDir Path tempDir) throws IOException {
		Path resourcePack = createResourcePack(tempDir, """
			{
			"textWidgetText": "0x112233",
			"navigationBackground": "0x80224466"
			}
			""");
		try (CloseableResourceManager resourceManager = createResourceManager(resourcePack)) {
			JeiGuiColors.onResourceManagerReload(resourceManager);
		}

		Assertions.assertEquals(0xFF112233, JeiGuiColors.getColor(GuiColor.TEXT_WIDGET_TEXT));
		Assertions.assertEquals(0x80224466, JeiGuiColors.getColor(GuiColor.NAVIGATION_BACKGROUND));
		Assertions.assertEquals(GuiColor.RECIPE_ERROR_TEXT.getDefaultColor(), JeiGuiColors.getColor(GuiColor.RECIPE_ERROR_TEXT));
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
		int packFormat = DetectedVersion.BUILT_IN
			.getPackVersion(com.mojang.bridge.game.PackType.RESOURCE);
		Files.writeString(resourcePack.resolve("pack.mcmeta"), """
			{
			"pack": {
				"pack_format": %d,
				"description": "JEI GUI color override test"
			}
			}
			""".formatted(packFormat));
		Files.writeString(resourcePack.resolve("assets/jei/gui/colors.json"), overrides);
		return resourcePack;
	}

	private static CloseableResourceManager createResourceManager(Path resourcePack) {
		PackResources packResources = new FolderPackResources(resourcePack.toFile());
		return new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(packResources));
	}
}
