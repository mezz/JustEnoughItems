package mezz.jei.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.library.config.ModIdFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ModIdFormatConfigTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	public void testDetectModNameTooltipFormattingWithNoModName() {
		// Setup: the tooltip has only the item name and no mod-name line.
		List<Component> tooltip = List.of(
			Component.literal("Apple")
		);

		// Operation: try to detect mod-name formatting even though there is no mod name.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: no override is detected when the mod name is absent.
		Assertions.assertEquals("", result);
	}

	@Test
	public void testDetectModNameTooltipFormattingIgnoresItemNameLine() {
		// Setup: the first tooltip line contains the Minecraft name, but there is no mod-name line.
		List<Component> tooltip = List.of(
			Component.literal(ModIds.MINECRAFT_NAME)
		);

		// Operation: try to detect formatting from a tooltip whose first line contains the mod name.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the detector ignores the first tooltip line.
		Assertions.assertEquals("", result);
	}

	@Test
	public void testDetectModNameTooltipFormattingUsesLaterModNameLine() {
		// Setup: a non-mod-name tooltip line appears before the styled mod-name line.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal("Food"),
			Component.literal(ModIds.MINECRAFT_NAME).withStyle(ChatFormatting.BLUE)
		);

		// Operation: detect formatting after skipping unrelated tooltip lines.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the later mod-name line is used.
		String expected = ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithLegacyFormatting() {
		// Setup: the mod-name line uses legacy formatting codes.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.BLUE + ModIds.MINECRAFT_NAME)
		);

		// Operation: detect formatting when the mod name is styled with legacy codes.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the legacy color code is preserved before the mod-name placeholder.
		String expected = ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithStyledComponent() {
		// Setup: the mod-name line uses component style instead of legacy formatting codes.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ModIds.MINECRAFT_NAME)
				.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
		);

		// Operation: detect formatting when the mod name is styled through the component.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the component style is converted to legacy codes before the placeholder.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithLegacyAndStyledComponent() {
		// Setup: the mod-name line combines legacy color with direct component style.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.BLUE + ModIds.MINECRAFT_NAME)
				.withStyle(ChatFormatting.ITALIC)
		);

		// Operation: detect formatting when legacy codes and component style both apply.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: both legacy and component styles are preserved before the placeholder.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingIgnoresStyleOutsideModName() {
		// Setup: the prefix and mod name use different component styles.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
				.append(Component.literal(ModIds.MINECRAFT_NAME).withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect formatting when non-mod-name text has its own style.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the prefix and mod-name styles are preserved.
		String expected = ChatFormatting.RED.toString() + "Mod: " + ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithStyleOnPrefixAndModNameSegment() {
		// Setup: part of the prefix is red and the rest of the line, including the mod name, is blue.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mod").withStyle(ChatFormatting.RED))
				.append(Component.literal("name: " + ModIds.MINECRAFT_NAME).withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect formatting when prefix text shares a component with the mod name.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the prefix and mod-name segment styles are preserved.
		String expected = ChatFormatting.RED.toString() + "Mod" + ChatFormatting.BLUE + "name: " + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithModNameSplitAcrossBlueComponents() {
		// Setup: the mod name is split across two separate blue components.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE))
				.append(Component.literal("craft").withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect formatting when the split mod-name components have matching color.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the shared blue style is applied to the placeholder.
		String expected = ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithModNameSplitAcrossStyledComponents() {
		// Setup: the mod name is split across components with the same style.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
				.append(Component.literal("craft").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
		);

		// Operation: detect formatting when the mod name spans multiple styled components.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: matching styles across all mod-name parts are applied to the placeholder.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingWithInconsistentModNameStyling() {
		// Setup: the mod name is split across components with different styles.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE))
				.append(Component.literal("craft").withStyle(ChatFormatting.ITALIC))
		);

		// Operation: detect formatting when different styles apply to parts of the mod name.
		String result = detectModNameTooltipFormatting(tooltip);

		// Assertions: inconsistent mod-name styling is not collapsed into one override style.
		Assertions.assertEquals(ModIdFormatConfig.MOD_NAME_FORMAT_CODE, result);
	}

	@Test
	public void testDetectModNameTooltipFormattingUsesPlatformTooltip() {
		// Setup: the platform item stack helper returns a tooltip with a styled mod-name line.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ModIds.MINECRAFT_NAME)
				.withStyle(ChatFormatting.BLUE)
		);

		// Operation: detect formatting through ModIdFormatConfig's platform-helper entry point.
		String result = ModIdFormatConfig.detectModNameTooltipFormatting(new TestItemStackHelper(tooltip));

		// Assertions: the platform tooltip is delegated to the formatting detector.
		String expected = ChatFormatting.BLUE + ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
		Assertions.assertEquals(expected, result);
	}

	private static String detectModNameTooltipFormatting(List<Component> tooltip) {
		return ModIdFormatConfig.detectModNameTooltipFormatting(new TestItemStackHelper(tooltip));
	}

	private record TestItemStackHelper(List<Component> tooltip) implements IPlatformItemStackHelper {
		@Override
		public int getBurnTime(ItemStack itemStack) {
			return 0;
		}

		@Override
		public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
			return false;
		}

		@Override
		public Optional<String> getCreatorModId(ItemStack stack) {
			return Optional.empty();
		}

		@Override
		public List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
			return tooltip;
		}
	}
}
