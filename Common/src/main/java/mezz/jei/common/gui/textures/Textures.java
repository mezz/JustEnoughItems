package mezz.jei.common.gui.textures;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.elements.DrawableSprite;
import mezz.jei.common.gui.elements.HighResolutionDrawable;
import mezz.jei.common.gui.elements.ScalableDrawable;
import net.minecraft.resources.Identifier;

public class Textures {
	private final JeiAtlasManager jeiAtlasManager;

	private final IDrawableStatic slot;
	private final IDrawableStatic outputSlot;
	private final ScalableDrawable recipeCatalystSlotBackground;
	private final ScalableDrawable ingredientListSlotBackground;
	private final ScalableDrawable bookmarkListSlotBackground;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final ScalableDrawable recipeGuiBackground;
	private final ScalableDrawable ingredientListBackground;
	private final ScalableDrawable bookmarkListBackground;
	private final ScalableDrawable recipeBackground;
	private final ScalableDrawable recipePreviewBackground;
	private final ScalableDrawable searchBackground;
	private final ScalableDrawable scrollbarBackground;
	private final ScalableDrawable scrollbarMarker;

	private final HighResolutionDrawable shapelessIcon;
	private final IDrawableStatic arrowPrevious;
	private final IDrawableStatic arrowNext;
	private final IDrawableStatic recipeTransfer;
	private final IDrawableStatic recipeBookmark;
	private final IDrawableStatic configButtonIcon;
	private final IDrawableStatic configButtonCheatIcon;
	private final IDrawableStatic bookmarkButtonDisabledIcon;
	private final IDrawableStatic bookmarkButtonEnabledIcon;
	private final IDrawableStatic historyButtonDisabledIcon;
	private final IDrawableStatic historyButtonEnabledIcon;
	private final IDrawableStatic infoIcon;
	private final ScalableDrawable catalystTab;
	private final ScalableDrawable recipeOptionsTab;
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

	private final IScalableDrawable buttonPressed;
	private final IScalableDrawable buttonPressedHighlight;

	public Textures(JeiAtlasManager jeiAtlasManager) {
		this.jeiAtlasManager = jeiAtlasManager;

		this.slot = createGuiSprite("slot");
		this.outputSlot = createGuiSprite("output_slot");
		this.recipeCatalystSlotBackground = createScalableGuiSprite("recipe_catalyst_slot_background");
		this.ingredientListSlotBackground = createScalableGuiSprite("ingredient_list_slot_background");
		this.bookmarkListSlotBackground = createScalableGuiSprite("bookmark_list_slot_background");
		this.tabSelected = createGuiSprite("tab_selected");
		this.tabUnselected = createGuiSprite("tab_unselected");
		this.recipeGuiBackground = createScalableGuiSprite("gui_background");
		this.ingredientListBackground = createScalableGuiSprite("ingredient_list_background");
		this.bookmarkListBackground = createScalableGuiSprite("bookmark_list_background");
		this.recipeBackground = createScalableGuiSprite("single_recipe_background");
		this.recipePreviewBackground = createScalableGuiSprite("recipe_preview_background");
		this.searchBackground = createScalableGuiSprite("search_background");
		this.scrollbarBackground = createScalableGuiSprite("scrollbar_background");
		this.scrollbarMarker = createScalableGuiSprite("scrollbar_marker");
		this.catalystTab = createScalableGuiSprite("catalyst_tab");
		this.recipeOptionsTab = createScalableGuiSprite("recipe_options_tab");
		this.recipeArrow = createGuiSprite("recipe_arrow");
		this.recipeArrowFilled = createGuiSprite("recipe_arrow_filled");
		this.recipePlusSign = createGuiSprite("recipe_plus_sign");

		this.brewingStandBackground = createGuiSprite("brewing_stand_background");
		this.brewingStandBlazeHeat = createGuiSprite("brewing_stand_blaze_heat");
		this.brewingStandBubbles = createGuiSprite("brewing_stand_bubbles");
		this.brewingStandArrow = createGuiSprite("brewing_stand_arrow");

		this.buttonPressed = createScalableGuiSprite("button_pressed");
		this.buttonPressedHighlight = createScalableGuiSprite("button_pressed_highlighted");

		IDrawableStatic rawShapelessIcon = createGuiSprite("icons/shapeless_icon");
		this.shapelessIcon = new HighResolutionDrawable(rawShapelessIcon, 4);

		this.arrowPrevious = createGuiSprite("icons/arrow_previous");
		this.arrowNext = createGuiSprite("icons/arrow_next");
		this.recipeTransfer = createGuiSprite("icons/recipe_transfer");
		this.recipeBookmark = createGuiSprite("icons/recipe_bookmark");
		this.configButtonIcon = createGuiSprite("icons/config_button");
		this.configButtonCheatIcon = createGuiSprite("icons/config_button_cheat");
		this.bookmarkButtonDisabledIcon = createGuiSprite("icons/bookmark_button_disabled");
		this.bookmarkButtonEnabledIcon = createGuiSprite("icons/bookmark_button_enabled");
		this.historyButtonDisabledIcon = createGuiSprite("icons/history_button_disabled");
		this.historyButtonEnabledIcon = createGuiSprite("icons/history_button_enabled");
		this.infoIcon = createGuiSprite("icons/info");
		this.flameIcon = createGuiSprite("icons/flame");
		this.flameEmptyIcon = createGuiSprite("icons/flame_empty");
		this.bookmarksFirst = createGuiSprite("icons/bookmarks_first");
		this.craftableFirst = createGuiSprite("icons/craftable_first");
	}

	private Identifier createSpriteId(String name) {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, name);
	}

	private IDrawableStatic createGuiSprite(String name) {
		Identifier id = createSpriteId(name);
		return new DrawableSprite(jeiAtlasManager.getAtlas(), id);
	}

	private ScalableDrawable createScalableGuiSprite(String name) {
		Identifier id = createSpriteId(name);
		return new ScalableDrawable(jeiAtlasManager.getAtlas(), id);
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

	public IDrawableStatic getHistoryButtonDisabledIcon() {
		return historyButtonDisabledIcon;
	}

	public IDrawableStatic getHistoryButtonEnabledIcon() {
		return historyButtonEnabledIcon;
	}

	public IDrawableStatic getBookmarkButtonEnabledIcon() {
		return bookmarkButtonEnabledIcon;
	}

	public ScalableDrawable getRecipeGuiBackground() {
		return recipeGuiBackground;
	}

	public ScalableDrawable getIngredientListBackground() {
		return ingredientListBackground;
	}

	public ScalableDrawable getBookmarkListBackground() {
		return bookmarkListBackground;
	}

	public ScalableDrawable getRecipeBackground() {
		return recipeBackground;
	}

	public ScalableDrawable getRecipePreviewBackground() {
		return recipePreviewBackground;
	}

	public ScalableDrawable getSearchBackground() {
		return searchBackground;
	}

	public IDrawableStatic getInfoIcon() {
		return infoIcon;
	}

	public ScalableDrawable getCatalystTab() {
		return catalystTab;
	}

	public ScalableDrawable getRecipeOptionsTab() {
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

	public ScalableDrawable getRecipeCatalystSlotBackground() {
		return recipeCatalystSlotBackground;
	}

	public ScalableDrawable getIngredientListSlotBackground() {
		return ingredientListSlotBackground;
	}

	public ScalableDrawable getBookmarkListSlotBackground() {
		return bookmarkListSlotBackground;
	}

	public IDrawableStatic getFlameIcon() {
		return flameIcon;
	}

	public IDrawableStatic getFlameEmptyIcon() {
		return flameEmptyIcon;
	}

	public ScalableDrawable getScrollbarMarker() {
		return scrollbarMarker;
	}

	public ScalableDrawable getScrollbarBackground() {
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

	public IScalableDrawable getButtonPressed() {
		return buttonPressed;
	}

	public IScalableDrawable getButtonPressedHighlight() {
		return buttonPressedHighlight;
	}

	public JeiAtlasManager getAtlasManager() {
		return jeiAtlasManager;
	}
}
