package mezz.jei.common.gui.textures;

import mezz.jei.common.gui.elements.HighResolutionDrawable;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.elements.DrawableSprite;

public class Textures {
	private final JeiSpriteUploader spriteUploader;

	private final IDrawableStatic slot;
	private final DrawableNineSliceTexture recipeCatalystSlotBackground;
	private final DrawableNineSliceTexture ingredientListSlotBackground;
	private final DrawableNineSliceTexture bookmarkListSlotBackground;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final DrawableNineSliceTexture buttonDisabled;
	private final DrawableNineSliceTexture buttonEnabled;
	private final DrawableNineSliceTexture buttonHighlight;
	private final DrawableNineSliceTexture recipeGuiBackground;
	private final DrawableNineSliceTexture ingredientListBackground;
	private final DrawableNineSliceTexture bookmarkListBackground;
	private final DrawableNineSliceTexture recipeBackground;
	private final DrawableNineSliceTexture searchBackground;

	private final HighResolutionDrawable shapelessIcon;
	private final IDrawableStatic arrowPrevious;
	private final IDrawableStatic arrowNext;
	private final IDrawableStatic recipeTransfer;
	private final IDrawableStatic configButtonIcon;
	private final IDrawableStatic configButtonCheatIcon;
	private final IDrawableStatic bookmarkButtonDisabledIcon;
	private final IDrawableStatic bookmarkButtonEnabledIcon;
	private final IDrawableStatic infoIcon;
	private final DrawableNineSliceTexture catalystTab;
	private final IDrawableStatic flameIcon;

	public Textures(JeiSpriteUploader spriteUploader) {
		this.spriteUploader = spriteUploader;

		this.slot = registerGuiSprite("slot", 18, 18);
		this.recipeCatalystSlotBackground = registerNineSliceGuiSprite("recipe_catalyst_slot_background", 18, 18, 4, 4, 4, 4);
		this.ingredientListSlotBackground = registerNineSliceGuiSprite("ingredient_list_slot_background", 18, 18, 4, 4, 4, 4);
		this.bookmarkListSlotBackground = registerNineSliceGuiSprite("bookmark_list_slot_background", 18, 18, 4, 4, 4, 4);
		this.tabSelected = registerGuiSprite("tab_selected", 24, 24);
		this.tabUnselected = registerGuiSprite("tab_unselected", 24, 24);
		this.buttonDisabled = registerNineSliceGuiSprite("button_disabled", 20, 20, 6, 6, 6, 6);
		this.buttonEnabled = registerNineSliceGuiSprite("button_enabled", 20, 20, 6, 6, 6, 6);
		this.buttonHighlight = registerNineSliceGuiSprite("button_highlight", 20, 20, 6, 6, 6, 6);
		this.recipeGuiBackground = registerNineSliceGuiSprite("gui_background", 64, 64, 16, 16, 16, 16);
		this.ingredientListBackground = registerNineSliceGuiSprite("ingredient_list_background", 64, 64, 16, 16, 16, 16);
		this.bookmarkListBackground = registerNineSliceGuiSprite("bookmark_list_background", 64, 64, 16, 16, 16, 16);
		this.recipeBackground = registerNineSliceGuiSprite("single_recipe_background", 64, 64, 16, 16, 16, 16);
		this.searchBackground = registerNineSliceGuiSprite("search_background", 20, 20, 6, 6, 6, 6);
		this.catalystTab = registerNineSliceGuiSprite("catalyst_tab", 28, 28, 8, 9, 8, 8);

		DrawableSprite rawShapelessIcon = registerGuiSprite("icons/shapeless_icon", 36, 36)
			.trim(1, 2, 1, 1);
		this.shapelessIcon = new HighResolutionDrawable(rawShapelessIcon, 4);

		this.arrowPrevious = registerGuiSprite("icons/arrow_previous", 9, 9)
			.trim(0, 0, 1, 1);
		this.arrowNext = registerGuiSprite("icons/arrow_next", 9, 9)
			.trim(0, 0, 1, 1);
		this.recipeTransfer = registerGuiSprite("icons/recipe_transfer", 7, 7);
		this.configButtonIcon = registerGuiSprite("icons/config_button", 16, 16);
		this.configButtonCheatIcon = registerGuiSprite("icons/config_button_cheat", 16, 16);
		this.bookmarkButtonDisabledIcon = registerGuiSprite("icons/bookmark_button_disabled", 16, 16);
		this.bookmarkButtonEnabledIcon = registerGuiSprite("icons/bookmark_button_enabled", 16, 16);
		this.infoIcon = registerGuiSprite("icons/info", 16, 16);
		this.flameIcon = registerGuiSprite("icons/flame", 14, 14);
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

	public IDrawableStatic getSlotDrawable() {
		return slot;
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
		if (!enabled) {
			return buttonDisabled;
		} else if (hovered) {
			return buttonHighlight;
		} else {
			return buttonEnabled;
		}
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

	public DrawableNineSliceTexture getSearchBackground() {
		return searchBackground;
	}

	public IDrawableStatic getInfoIcon() {
		return infoIcon;
	}

	public DrawableNineSliceTexture getCatalystTab() {
		return catalystTab;
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
}
