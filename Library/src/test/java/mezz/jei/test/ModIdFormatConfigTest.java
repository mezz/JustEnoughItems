package mezz.jei.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.library.config.ModIdFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ModIdFormatConfigTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void testDetectModNameTooltipFormattingWithNoModName() {
		List<Component> tooltip = List.of(
			Component.literal("Apple")
		);

		String result = ModIdFormatConfig.detectModNameTooltipFormatting(tooltip);

		Assertions.assertEquals("", result);
	}

	@Test
	public void testDetectModNameTooltipFormattingIgnoresItemNameLine() {
		List<Component> tooltip = List.of(
			Component.literal(ModIds.MINECRAFT_NAME)
		);

		String result = ModIdFormatConfig.detectModNameTooltipFormatting(tooltip);

		Assertions.assertEquals("", result);
	}

	@Test
	public void testDetectModNameTooltipFormattingUsesLaterModNameLine() {
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal("Food"),
			Component.literal(ChatFormatting.BLUE + ModIds.MINECRAFT_NAME)
		);

		String result = ModIdFormatConfig.detectModNameTooltipFormatting(tooltip);

		String expected = ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithPrefix() {
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.RED + "Mod: " + ChatFormatting.BLUE + ModIds.MINECRAFT_NAME)
		);

		String result = ModIdFormatConfig.detectModNameTooltipFormatting(tooltip);

		String expected = ChatFormatting.RED + "Mod: " + ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}
}
