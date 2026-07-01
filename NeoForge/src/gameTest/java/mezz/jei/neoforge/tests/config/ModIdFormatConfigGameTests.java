package mezz.jei.neoforge.tests.config;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import mezz.jei.library.helpers.ModIdHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ForEachTest(groups = "mod_id_format_config")
public final class ModIdFormatConfigGameTests {
	private static final String MOD_NAME = ModIds.MINECRAFT_NAME;
	private static final String MOD_NAME_FORMAT_CODE = ModIdFormatConfig.MOD_NAME_FORMAT_CODE;

	private ModIdFormatConfigGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not detect a mod-name format when the tooltip has no mod name.")
	public static void detectsNoFormatWhenTooltipHasNoModName(JeiGameTestHelper helper) {
		// Setup: the tooltip has only the item name and no mod-name line.
		List<Component> tooltip = List.of(
			Component.literal("Apple")
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: no external mod-name format is detected.
		assertComponentEquals(helper, Component.empty(), result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not treat the item-name line as a mod-name format.")
	public static void ignoresModNameTextOnItemNameLine(JeiGameTestHelper helper) {
		// Setup: the first tooltip line contains "Minecraft" but the later tooltip line does not.
		List<Component> tooltip = List.of(
			Component.literal(MOD_NAME),
			Component.literal("No mod name here")
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the first tooltip line is ignored because it is the item name.
		assertComponentEquals(helper, Component.empty(), result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects an unstyled mod-name tooltip line.")
	public static void detectsUnstyledModNameLine(JeiGameTestHelper helper) {
		// Setup: the second tooltip line is exactly the Minecraft mod name with no styling.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(MOD_NAME)
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the mod name is replaced by the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects a mod-name tooltip line styled with legacy formatting codes.")
	public static void detectsLegacyStyledModNameLine(JeiGameTestHelper helper) {
		// Setup: the mod-name line uses legacy blue formatting in the literal text.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.BLUE + MOD_NAME)
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the legacy color is preserved on the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(ChatFormatting.BLUE);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects a mod-name tooltip line styled with modern component styling.")
	public static void detectsComponentStyledModNameLine(JeiGameTestHelper helper) {
		// Setup: the mod-name line uses component style instead of legacy formatting codes.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(MOD_NAME)
				.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the component style is preserved on the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects a mod-name tooltip line with both legacy and component styling.")
	public static void detectsLegacyAndComponentStyledModNameLine(JeiGameTestHelper helper) {
		// Setup: the mod-name line combines legacy blue text with component italic styling.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.BLUE + MOD_NAME)
				.withStyle(ChatFormatting.ITALIC)
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: both styling sources are preserved on the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Preserves component styling on tooltip text before the mod name.")
	public static void preservesComponentStyleOnPrefixBeforeModName(JeiGameTestHelper helper) {
		// Setup: the tooltip prefix is red and the mod name is blue.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
				.append(Component.literal(MOD_NAME).withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: prefix styling is preserved and the mod name is replaced.
		Component expected = Component.empty()
			.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(MOD_NAME_FORMAT_CODE).withStyle(ChatFormatting.BLUE));
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Preserves component styling when prefix text shares a segment with the mod name.")
	public static void preservesComponentStyleOnPrefixSegmentContainingModName(JeiGameTestHelper helper) {
		// Setup: part of the prefix is red, and the remaining prefix plus mod name are blue.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mod").withStyle(ChatFormatting.RED))
				.append(Component.literal("name: " + MOD_NAME).withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the blue component text before the mod name is kept with its style.
		Component expected = Component.empty()
			.append(Component.literal("Mod").withStyle(ChatFormatting.RED))
			.append(Component.literal("name: " + MOD_NAME_FORMAT_CODE).withStyle(ChatFormatting.BLUE));
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects a split mod name when every part has the same component color.")
	public static void detectsSplitModNameWithMatchingComponentColor(JeiGameTestHelper helper) {
		// Setup: the mod name is split across two blue components.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE))
				.append(Component.literal("craft").withStyle(ChatFormatting.BLUE))
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the shared blue style is applied to the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(ChatFormatting.BLUE);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Detects a split mod name when every part has the same component style.")
	public static void detectsSplitModNameWithMatchingComponentStyle(JeiGameTestHelper helper) {
		// Setup: the mod name is split across two components with matching blue italic style.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
				.append(Component.literal("craft").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: the shared blue italic style is applied to the placeholder.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Drops mod-name styling when split components use different styles.")
	public static void dropsSplitModNameStyleWhenComponentStylesDiffer(JeiGameTestHelper helper) {
		// Setup: each part of the split mod name uses a different component style.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.empty()
				.append(Component.literal("Mine").withStyle(ChatFormatting.BLUE))
				.append(Component.literal("craft").withStyle(ChatFormatting.ITALIC))
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: inconsistent mod-name styling is not collapsed into one placeholder style.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Drops mod-name styling when split legacy formatting codes use different styles.")
	public static void dropsSplitModNameStyleWhenLegacyStylesDiffer(JeiGameTestHelper helper) {
		// Setup: the mod name is split by legacy codes that change style halfway through.
		List<Component> tooltip = List.of(
			Component.literal("Apple"),
			Component.literal(ChatFormatting.BLUE + "Mine" + ChatFormatting.ITALIC + "craft")
		);

		// Operation: detect the external mod-name tooltip format.
		Component result = detectModNameTooltipFormatting(tooltip);

		// Assertions: inconsistent legacy styling is not collapsed into one placeholder style.
		Component expected = Component.literal(MOD_NAME_FORMAT_CODE);
		assertComponentEquals(helper, expected, result, "Detected mod-name tooltip formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Applies component mod-name formatting to the component mod-name API.")
	public static void modIdHelperAppliesComponentFormattingToComponentApi(JeiGameTestHelper helper) {
		// Setup: ModIdHelper has a component format with red prefix text and blue italic placeholder text.
		ModIdHelper modIdHelper = createModIdHelper(helper);

		// Operation: format the Minecraft mod name through the component API.
		Component result = modIdHelper.getFormattedModNameComponentForModId(ModIds.MINECRAFT_ID);

		// Assertions: the returned component preserves the configured component formatting.
		Component expected = Component.empty()
			.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(MOD_NAME).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
		assertComponentEquals(helper, expected, result, "ModIdHelper should apply component formatting");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Applies component mod-name formatting to the legacy formatted mod-name API.")
	@SuppressWarnings("removal")
	public static void modIdHelperAppliesComponentFormattingToLegacyStringApi(JeiGameTestHelper helper) {
		// Setup: ModIdHelper has a component format with red prefix text and blue italic placeholder text.
		ModIdHelper modIdHelper = createModIdHelper(helper);

		// Operation: format the Minecraft mod name through the legacy string API.
		String result = modIdHelper.getFormattedModNameForModId(ModIds.MINECRAFT_ID);

		// Assertions: the returned string preserves the component formatting as legacy codes.
		String expected = ChatFormatting.RED + "Mod: " + ChatFormatting.BLUE + "" + ChatFormatting.ITALIC + MOD_NAME;
		helper.assertEquals(expected, result, "ModIdHelper should apply component formatting before legacy serialization");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Uses the injected display-mod-id function when formatting tooltip mod names.")
	public static void modIdHelperUsesDisplayModIdFunctionForTooltipModName(JeiGameTestHelper helper) {
		// Setup: a typed item stack and a ModIdHelper with an injected display-mod-id function.
		ModIdHelper modIdHelper = createModIdHelper(helper);
		ITypedIngredient<ItemStack> typedIngredient = new TestTypedIngredient<>(VanillaTypes.ITEM_STACK, new ItemStack(Items.APPLE));

		// Operation: request the formatted mod-name component for a tooltip.
		Component tooltipModName = modIdHelper.getModNameForTooltip(typedIngredient)
			.orElseThrow(() -> helper.createFailException("Expected mod name tooltip component"));

		// Assertions: the display-mod-id function is used and component styling is preserved.
		Component expected = Component.empty()
			.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(MOD_NAME).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
		assertComponentEquals(helper, expected, tooltipModName, "ModIdHelper should use the display mod id function for tooltips");
		helper.succeed();
	}

	private static ModIdHelper createModIdHelper(JeiGameTestHelper helper) {
		Component modNameFormat = Component.empty()
			.append(Component.literal("Mod: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(MOD_NAME_FORMAT_CODE).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
		IModIdFormatConfig config = new TestModIdFormatConfig(modNameFormat);
		return new ModIdHelper(
			config,
			typedIngredient -> {
				helper.assertEquals(VanillaTypes.ITEM_STACK, typedIngredient.getType(), "ModIdHelper should pass the typed ingredient to the display mod id function");
				return ModIds.MINECRAFT_ID;
			},
			ImmutableSetMultimap.of()
		);
	}

	private static Component detectModNameTooltipFormatting(List<Component> tooltip) {
		return ModIdFormatConfig.detectModNameTooltipFormatting(new TestItemStackHelper(tooltip), null);
	}

	private static void assertComponentEquals(JeiGameTestHelper helper, Component expected, Component result, String message) {
		List<StyledText> expectedText = getStyledTexts(expected);
		List<StyledText> resultText = getStyledTexts(result);
		helper.assertEquals(expectedText, resultText, message);
	}

	private static List<StyledText> getStyledTexts(Component component) {
		List<StyledText> styledTexts = new ArrayList<>();
		component.visit((style, text) -> {
			if (!text.isEmpty()) {
				addStyledText(styledTexts, text, style);
			}
			return Optional.empty();
		}, Style.EMPTY);
		return styledTexts;
	}

	private static void addStyledText(List<StyledText> styledTexts, String text, Style style) {
		if (!styledTexts.isEmpty()) {
			StyledText previous = styledTexts.getLast();
			if (Objects.equals(previous.style(), style)) {
				styledTexts.set(styledTexts.size() - 1, new StyledText(previous.text() + text, style));
				return;
			}
		}
		styledTexts.add(new StyledText(text, style));
	}

	private record StyledText(String text, Style style) {
	}

	private record TestItemStackHelper(List<Component> tooltip) implements IPlatformItemStackHelper {
		@Override
		public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType, FuelValues fuelValues) {
			return 0;
		}

		@Override
		public Optional<String> getCreatorModId(ItemStack stack) {
			return Optional.empty();
		}

		@Override
		public List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
			return tooltip;
		}

		@Override
		public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
			return false;
		}
	}

	private record TestModIdFormatConfig(Component modNameFormat) implements IModIdFormatConfig {
		@Override
		public Component getModNameFormat() {
			return modNameFormat;
		}

		@Override
		public boolean isModNameFormatOverrideActive() {
			return false;
		}
	}

	private record TestTypedIngredient<T>(IIngredientType<T> type, T ingredient) implements ITypedIngredient<T> {
		@Override
		public IIngredientType<T> getType() {
			return type;
		}

		@Override
		public T getIngredient() {
			return ingredient;
		}
	}
}
