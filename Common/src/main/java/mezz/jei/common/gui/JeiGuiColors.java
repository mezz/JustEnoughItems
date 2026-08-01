package mezz.jei.common.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import mezz.jei.api.constants.ModIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

public final class JeiGuiColors {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Identifier COLORS_RESOURCE = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "gui/colors.json");
	private static final long MAX_COLOR = 0xFFFFFFFFL;

	private static volatile Map<GuiColor, Integer> colors = createDefaultColors();

	private JeiGuiColors() {

	}

	public static void onResourceManagerReload(ResourceManager resourceManager) {
		Map<GuiColor, Integer> loadedColors = createDefaultColors();
		for (Resource resource : resourceManager.getResourceStack(COLORS_RESOURCE)) {
			try (Reader reader = resource.openAsReader()) {
				JsonElement jsonElement = JsonParser.parseReader(reader);
				loadColors(jsonElement, loadedColors);
			} catch (IOException | RuntimeException e) {
				LOGGER.error("Failed to load JEI GUI colors from resource: {}", COLORS_RESOURCE, e);
			}
		}
		colors = Map.copyOf(loadedColors);
	}

	public static int getColor(GuiColor color) {
		return colors.getOrDefault(color, color.defaultColor);
	}

	private static Map<GuiColor, Integer> createDefaultColors() {
		Map<GuiColor, Integer> defaults = new EnumMap<>(GuiColor.class);
		for (GuiColor color : GuiColor.values()) {
			defaults.put(color, color.defaultColor);
		}
		return defaults;
	}

	private static void loadColors(JsonElement jsonElement, Map<GuiColor, Integer> loadedColors) {
		if (!jsonElement.isJsonObject()) {
			LOGGER.error("JEI GUI colors resource must be a JSON object: {}", COLORS_RESOURCE);
			return;
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (GuiColor color : GuiColor.values()) {
			JsonElement colorJson = jsonObject.get(color.key);
			if (colorJson != null) {
				OptionalInt colorValue = parseColor(colorJson);
				if (colorValue.isPresent()) {
					loadedColors.put(color, colorValue.getAsInt());
				} else {
					LOGGER.error("Invalid JEI GUI color '{}' in resource '{}': {}", color.key, COLORS_RESOURCE, colorJson);
				}
			}
		}
	}

	static OptionalInt parseColor(JsonElement colorJson) {
		if (!colorJson.isJsonPrimitive()) {
			return OptionalInt.empty();
		}
		JsonPrimitive primitive = colorJson.getAsJsonPrimitive();
		if (!primitive.isString()) {
			return OptionalInt.empty();
		}
		return parseColorString(primitive.getAsString());
	}

	static OptionalInt parseColorString(String string) {
		string = string.trim();
		if (!string.toLowerCase(Locale.ROOT).startsWith("0x")) {
			return OptionalInt.empty();
		}
		string = string.substring(2);
		if (string.length() == 6) {
			string = "FF" + string;
		}
		if (string.length() != 8) {
			return OptionalInt.empty();
		}
		try {
			long value = Long.parseUnsignedLong(string, 16);
			if (value <= MAX_COLOR) {
				return OptionalInt.of((int) value);
			}
		} catch (NumberFormatException ignored) {

		}
		return OptionalInt.empty();
	}

	public enum GuiColor {
		RECIPE_TEXT_WIDGET_TEXT("recipeTextWidgetText", 0xFF000000),
		RECIPE_SCROLL_BOX_TEXT("recipeScrollBoxText", 0xFF000000),
		ANVIL_EXPERIENCE_COST_TEXT("anvilExperienceCostText", 0xFF80FF20),
		ANVIL_EXPERIENCE_COST_ERROR_TEXT("anvilExperienceCostErrorText", 0xFFFF6060),
		GRINDSTONE_EXPERIENCE_REWARD_TEXT("grindstoneExperienceRewardText", 0xFF80FF20),
		RECIPE_BREWING_STEPS_TEXT("recipeBrewingStepsText", 0xFF808080),
		RECIPE_COMPOSTING_CHANCE_TEXT("recipeCompostingChanceText", 0xFF808080),
		RECIPE_COOKING_EXPERIENCE_TEXT("recipeCookingExperienceText", 0xFF808080),
		RECIPE_COOKING_TIME_TEXT("recipeCookingTimeText", 0xFF808080),
		RECIPE_FUEL_SMELT_COUNT_TEXT("recipeFuelSmeltCountText", 0xFF808080),
		TAG_INFORMATION_TEXT("tagInformationText", 0xFF505050),
		TAG_INFORMATION_IDENTIFIER_TEXT("tagInformationIdentifierText", 0xFFAAAAAA),
		RECIPE_CATEGORY_ICON_TEXT("recipeCategoryIconText", 0xFFE0E0E0),
		RECIPE_CATEGORY_TITLE_TEXT("recipeCategoryTitleText", 0xFFFFFFFF),
		SEARCH_FIELD_TEXT("searchFieldText", 0xFFFFFFFF),
		SEARCH_FIELD_ERROR_TEXT("searchFieldErrorText", 0xFFFF0000),
		LOOKUP_HISTORY_LINE("lookupHistoryLine", 0xFF959595),
		PAGE_NAVIGATION_BACKGROUND("pageNavigationBackground", 0x30000000),
		PAGE_NAVIGATION_TEXT("pageNavigationText", 0xFFFFFFFF),
		BOOKMARKED_RECIPE_OVERLAY("bookmarkedRecipeOverlay", 0x1100FF00),
		EDIT_MODE_HIDDEN_INGREDIENT_OVERLAY("editModeHiddenIngredientOverlay", 0xDDFF0000),
		EDIT_MODE_HIDDEN_WILDCARD_OVERLAY("editModeHiddenWildcardOverlay", 0xDDFFA500),
		GHOST_INGREDIENT_DRAG_TARGET_HIGHLIGHT("ghostIngredientDragTargetHighlight", 0x4013C90A),
		GHOST_INGREDIENT_DRAG_HOVER_HIGHLIGHT("ghostIngredientDragHoverHighlight", 0x804CC919),
		TAG_CONTENT_TOOLTIP_COUNT_TEXT("tagContentTooltipCountText", 0xFFAAAAAA),
		INGREDIENT_RENDER_ERROR_TEXT("ingredientRenderErrorText", 0xFFFF0000),
		RECIPE_TRANSFER_MISSING_SLOT_HIGHLIGHT("recipeTransferMissingSlotHighlight", 0x66FF0000),
		RECIPE_TRANSFER_BUTTON_HIGHLIGHT("recipeTransferButtonHighlight", 0x80FFA500),
		DEBUG_WIDGET_AREA("debugWidgetArea", 0xAAAAAA00),
		DEBUG_GUI_EXCLUSION_AREA("debugGuiExclusionArea", 0x44FF0000),
		DEBUG_GUI_AREA("debugGuiArea", 0x22CCCC00),
		DEBUG_RECIPE_GUI_IDEAL_AREA("debugRecipeGuiIdealArea", 0x4400FF00),
		DEBUG_RECIPE_GUI_AREA("debugRecipeGuiArea", 0x44990044),
		DEBUG_RECIPE_LAYOUTS_AREA("debugRecipeLayoutsArea", 0x44228844);

		private final String key;
		private final int defaultColor;

		GuiColor(String key, int defaultColor) {
			this.key = key;
			this.defaultColor = defaultColor;
		}

		@SuppressWarnings("unused") // used by data generation
		String getKey() {
			return key;
		}

		@SuppressWarnings("unused") // used by data generation
		String getDefaultColorString() {
			return "0x%08X".formatted(defaultColor);
		}

		public int getDefaultColor() {
			return defaultColor;
		}
	}
}
