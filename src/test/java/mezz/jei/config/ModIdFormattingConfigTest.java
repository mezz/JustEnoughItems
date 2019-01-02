package mezz.jei.config;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.common.config.Property;
import net.minecraft.util.text.TextFormatting;

import org.junit.Assert;
import org.junit.Test;

public class ModIdFormattingConfigTest extends ForgeConfigTestBase {

	@Test
	public void independentFilesRetainTheirFormattingAcrossReloads() throws IOException {
		File firstFile = temporaryFolder.newFile("first.cfg");
		File secondFile = temporaryFolder.newFile("second.cfg");
		LocalizedConfiguration first = new LocalizedConfiguration("config.jei", firstFile, "0.4.0");
		LocalizedConfiguration second = new LocalizedConfiguration("config.jei", secondFile, "0.4.0");
		first.get(Config.CATEGORY_ADVANCED, "modNameFormat", "").set("red bold");
		second.get(Config.CATEGORY_ADVANCED, "modNameFormat", "").set("");
		first.save();
		second.save();
		ModIdFormattingConfig firstFormatting = new ModIdFormattingConfig();
		ModIdFormattingConfig secondFormatting = new ModIdFormattingConfig();

		firstFormatting.sync(new LocalizedConfiguration("config.jei", firstFile, "0.4.0"));
		secondFormatting.sync(new LocalizedConfiguration("config.jei", secondFile, "0.4.0"));

		Assert.assertEquals(TextFormatting.RED.toString() + TextFormatting.BOLD, firstFormatting.getModNameFormat());
		Assert.assertEquals("", secondFormatting.getModNameFormat());
	}

	@Test
	public void detectedOverrideHidesTheSettingWithoutOverwritingItsSavedValue() throws IOException {
		File file = temporaryFolder.newFile("jei.cfg");
		LocalizedConfiguration config = new LocalizedConfiguration("config.jei", file, "0.4.0");
		Property property = config.get(Config.CATEGORY_ADVANCED, "modNameFormat", "blue italic");
		property.set("gold");
		ModIdFormattingConfig formatting = new ModIdFormattingConfig();
		formatting.sync(config);

		formatting.setModNameFormatOverride(TextFormatting.GREEN + "[" + ModIdFormattingConfig.MOD_NAME_FORMAT_CODE + "]");
		config.save();

		Assert.assertEquals(TextFormatting.GREEN + "[%MODNAME%]", formatting.getModNameFormat());
		Assert.assertFalse(property.showInGui());
		Assert.assertEquals("gold", new LocalizedConfiguration("config.jei", file, "0.4.0").get(Config.CATEGORY_ADVANCED, "modNameFormat", "").getString());

		formatting.setModNameFormatOverride(null);

		Assert.assertEquals(TextFormatting.GOLD.toString(), formatting.getModNameFormat());
		Assert.assertTrue(property.showInGui());
	}
}
