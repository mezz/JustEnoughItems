package mezz.jei.common.gui.textures;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.elements.DrawableSprite;
import mezz.jei.common.gui.elements.HighResolutionDrawable;
import net.minecraft.resources.ResourceLocation;

public class Textures {
	private final JeiSpriteUploader spriteUploader;

	private final IDrawableStatic slot;
	private final IDrawableStatic outputSlot;
	private final DrawableNineSliceTexture recipeCatalystSlotBackground;
	private final DrawableNineSliceTexture ingredientListSlotBackground;
	private final DrawableNineSliceTexture bookmarkListSlotBackground;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final DrawableNineSliceTexture buttonDisabled;
	private final DrawableNineSliceTexture buttonEnabled;
	private final DrawableNineSliceTexture buttonHighlight;
	private final DrawableNineSliceTexture buttonPressed;
	private final DrawableNineSliceTexture buttonPressedHighlight;
	private final DrawableNineSliceTexture recipeGuiBackground;
	private final DrawableNineSliceTexture ingredientListBackground;
	private final DrawableNineSliceTexture bookmarkListBackground;
	private final DrawableNineSliceTexture recipeBackground;
	private final DrawableNineSliceTexture recipePreviewBackground;
	private final DrawableNineSliceTexture searchBackground;
	private final DrawableNineSliceTexture scrollbarBackground;
	private final DrawableNineSliceTexture scrollbarMarker;

	private final HighResolutionDrawable shapelessIcon;
	private final IDrawableStatic arrowPrevious;
	private final IDrawableStatic arrowNext;
	private final IDrawableStatic recipeTransfer;
	private final IDrawableStatic recipeBookmark;
	private final IDrawableStatic configButtonIcon;
	private final IDrawableStatic configButtonCheatIcon;
	private final IDrawableStatic bookmarkButtonDisabledIcon;
	private final IDrawableStatic bookmarkButtonEnabledIcon;
	private final IDrawableStatic infoIcon;
	private final DrawableNineSliceTexture catalystTab;
	private final DrawableNineSliceTexture recipeOptionsTab;
	private final IDrawableStatic flameIcon;
	private final IDrawableStatic flameEmptyIcon;
	private final IDrawableStatic recipeArrow;
	private final IDrawableStatic recipeArrowFilled;
	private final IDrawableStatic recipePlusSign;
	private final IDrawableStatic bookmarksFirst;
	private final IDrawableStatic craftableFirst;

	private final IDrawableStatic brewingStandBackground;
	private final IDrawableStatic brewingStandBlazeHeat;
	private final IDrawableStatic brewingStandBubbles;
	private final IDrawableStatic brewingStandArrow;

	public Textures(JeiSpriteUploader spriteUploader) {
		this.spriteUploader = spriteUploader;

		this.slot = registerGuiSprite("slot", 18, 18);
		this.outputSlot = registerGuiSprite("output_slot", 26, 26);
		this.recipeCatalystSlotBackground = registerNineSliceGuiSprite("recipe_catalyst_slot_background", 18, 18, 4, 4, 4, 4);
		this.ingredientListSlotBackground = registerNineSliceGuiSprite("ingredient_list_slot_background", 18, 18, 4, 4, 4, 4);
		this.bookmarkListSlotBackground = registerNineSliceGuiSprite("bookmark_list_slot_background", 18, 18, 4, 4, 4, 4);
		this.tabSelected = registerGuiSprite("tab_selected", 24, 24);
		this.tabUnselected = registerGuiSprite("tab_unselected", 24, 24);
		this.buttonDisabled = registerNineSliceGuiSprite("button_disabled", 20, 20, 6, 6, 6, 6);
		this.buttonEnabled = registerNineSliceGuiSprite("button_enabled", 20, 20, 6, 6, 6, 6);
		this.buttonHighlight = registerNineSliceGuiSprite("button_highlight", 20, 20, 6, 6, 6, 6);
		this.buttonPressed = registerNineSliceGuiSprite("button_pressed", 20, 20, 6, 6, 6, 6);
		this.buttonPressedHighlight = registerNineSliceGuiSprite("button_pressed_highlight", 20, 20, 6, 6, 6, 6);
		this.recipeGuiBackground = registerNineSliceGuiSprite("gui_background", 64, 64, 16, 16, 16, 16);
		this.ingredientListBackground = registerNineSliceGuiSprite("ingredient_list_background", 64, 64, 16, 16, 16, 16);
		this.bookmarkListBackground = registerNineSliceGuiSprite("bookmark_list_background", 64, 64, 16, 16, 16, 16);
		this.recipeBackground = registerNineSliceGuiSprite("single_recipe_background", 64, 64, 16, 16, 16, 16);
		this.recipePreviewBackground = registerNineSliceGuiSprite("recipe_preview_background", 64, 64, 16, 16, 16, 16);
		this.searchBackground = registerNineSliceGuiSprite("search_background", 20, 20, 6, 6, 6, 6);
		this.scrollbarBackground = registerNineSliceGuiSprite("scrollbar_background", 14, 50, 6, 6, 6, 6);
		this.scrollbarMarker = registerNineSliceGuiSprite("scrollbar_marker", 12, 15, 2, 2, 2, 1);
		this.catalystTab = registerNineSliceGuiSprite("catalyst_tab", 28, 28, 8, 9, 8, 8);
		this.recipeOptionsTab = registerNineSliceGuiSprite("recipe_options_tab", 28, 28, 8, 9, 8, 8);
		this.recipeArrow = registerGuiSprite("recipe_arrow", 22, 16);
		this.recipeArrowFilled = registerGuiSprite("recipe_arrow_filled", 22, 16);
		this.recipePlusSign = registerGuiSprite("recipe_plus_sign", 13, 13);

		this.brewingStandBackground = registerGuiSprite("brewing_stand_background", 64, 60);
		this.brewingStandBlazeHeat = registerGuiSprite("brewing_stand_blaze_heat", 18, 4);
		this.brewingStandBubbles = registerGuiSprite("brewing_stand_bubbles", 11, 28);
		this.brewingStandArrow = registerGuiSprite("brewing_stand_arrow", 7, 27);

		DrawableSprite rawShapelessIcon = registerGuiSprite("icons/shapeless_icon", 36, 36)
			.trim(1, 2, 1, 1);
		this.shapelessIcon = new HighResolutionDrawable(rawShapelessIcon, 4);

		this.arrowPrevious = registerGuiSprite("icons/arrow_previous", 9, 9)
			.trim(0, 0, 1, 1);
		this.arrowNext = registerGuiSprite("icons/arrow_next", 9, 9)
			.trim(0, 0, 1, 1);
		this.recipeTransfer = registerGuiSprite("icons/recipe_transfer", 7, 7);
		this.recipeBookmark = registerGuiSprite("icons/recipe_bookmark", 9, 9);
		this.configButtonIcon = registerGuiSprite("icons/config_button", 16, 16);
		this.configButtonCheatIcon = registerGuiSprite("icons/config_button_cheat", 16, 16);
		this.bookmarkButtonDisabledIcon = registerGuiSprite("icons/bookmark_button_disabled", 16, 16);
		this.bookmarkButtonEnabledIcon = registerGuiSprite("icons/bookmark_button_enabled", 16, 16);
		this.infoIcon = registerGuiSprite("icons/info", 16, 16);
		this.flameIcon = registerGuiSprite("icons/flame", 14, 14);
		this.flameEmptyIcon = registerGuiSprite("icons/flame_empty", 14, 14);
		this.bookmarksFirst = registerGuiSprite("icons/bookmarks_first", 16, 16);
		this.craftableFirst = registerGuiSprite("icons/craftable_first", 16, 16);
	}

	private ResourceLocation registerSprite(String name) {
		ResourceLocation location = new ResourceLocation(ModIds.JEI_ID, name);
		spriteUploader.registerSprite(location);
		return location;
	}

	private DrawableSprite registerGuiSprite(String name, int width, int height) {
		ResourceLocation location = registerSprite(name);
		return new DrawableSprite(spriteUploader, location, width, height);
	}

	private DrawableNineSliceTexture registerNineSliceGuiSprite(String name, int width, int height, int left, int right, int top, int bottom) {
		ResourceLocation location = registerSprite(name);
		return new DrawableNineSliceTexture(spriteUploader, location, width, height, left, right, top, bottom);
	}

	public IDrawableStatic getSlot() {
		return slot;
	}

	public IDrawableStatic getOutputSlot() {
		return outputSlot;
	}

	public IDrawableStatic getTabSelected() {
		return tabSelected;
	}

	public IDrawableStatic getTabUnselected() {
		return tabUnselected;
	}

	public HighResolutionDrawable getShapelessIcon() {
		return shapelessIcon;
	}

	public IDrawableStatic getArrowPrevious() {
		return arrowPrevious;
	}

	public IDrawableStatic getArrowNext() {
		return arrowNext;
	}

	public IDrawableStatic getRecipeTransfer() {
		return recipeTransfer;
	}

	public IDrawableStatic getRecipeBookmark() {
		return recipeBookmark;
	}

	public IDrawableStatic getBookmarksFirst() {
		return bookmarksFirst;
	}

	public IDrawableStatic getCraftableFirst() {
		return craftableFirst;
	}

	public IDrawableStatic getConfigButtonIcon() {
		return configButtonIcon;
	}

	public IDrawableStatic getConfigButtonCheatIcon() {
		return configButtonCheatIcon;
	}

	public IDrawableStatic getBookmarkButtonDisabledIcon() {
		return bookmarkButtonDisabledIcon;
	}

	public IDrawableStatic getBookmarkButtonEnabledIcon() {
		return bookmarkButtonEnabledIcon;
	}

	public DrawableNineSliceTexture getButtonForState(boolean enabled, boolean hovered) {
		return getButtonForState(false, enabled, hovered);
	}

	public DrawableNineSliceTexture getButtonForState(boolean pressed, boolean enabled, boolean hovered) {
		if (!enabled) {
			return buttonDisabled;
		}

		if (hovered) {
			return pressed ? buttonPressedHighlight : buttonHighlight;
		}
		return pressed ? buttonPressed : buttonEnabled;
	}

	public DrawableNineSliceTexture getRecipeGuiBackground() {
		return recipeGuiBackground;
	}

	public DrawableNineSliceTexture getIngredientListBackground() {
		return ingredientListBackground;
	}

	public DrawableNineSliceTexture getBookmarkListBackground() {
		return bookmarkListBackground;
	}

	public DrawableNineSliceTexture getRecipeBackground() {
		return recipeBackground;
	}

	public DrawableNineSliceTexture getRecipePreviewBackground() {
		return recipePreviewBackground;
	}

	public DrawableNineSliceTexture getSearchBackground() {
		return searchBackground;
	}

	public IDrawableStatic getInfoIcon() {
		return infoIcon;
	}

	public DrawableNineSliceTexture getCatalystTab() {
		return catalystTab;
	}

	public DrawableNineSliceTexture getRecipeOptionsTab() {
		return recipeOptionsTab;
	}

	public IDrawableStatic getRecipeArrow() {
		return recipeArrow;
	}

	public IDrawableStatic getRecipeArrowFilled() {
		return recipeArrowFilled;
	}

	public IDrawableStatic getRecipePlusSign() {
		return recipePlusSign;
	}

	public DrawableNineSliceTexture getRecipeCatalystSlotBackground() {
		return recipeCatalystSlotBackground;
	}

	public DrawableNineSliceTexture getIngredientListSlotBackground() {
		return ingredientListSlotBackground;
	}

	public DrawableNineSliceTexture getBookmarkListSlotBackground() {
		return bookmarkListSlotBackground;
	}

	public IDrawableStatic getFlameIcon() {
		return flameIcon;
	}

	public IDrawableStatic getFlameEmptyIcon() {
		return flameEmptyIcon;
	}

	public DrawableNineSliceTexture getScrollbarMarker() {
		return scrollbarMarker;
	}

	public DrawableNineSliceTexture getScrollbarBackground() {
		return scrollbarBackground;
	}

	public IDrawableStatic getBrewingStandBackground() {
		return brewingStandBackground;
	}

	public IDrawableStatic getBrewingStandBlazeHeat() {
		return brewingStandBlazeHeat;
	}

	public IDrawableStatic getBrewingStandBubbles() {
		return brewingStandBubbles;
	}

	public IDrawableStatic getBrewingStandArrow() {
		return brewingStandArrow;
	}

	public JeiSpriteUploader getSpriteUploader() {
		return spriteUploader;
	}
}
