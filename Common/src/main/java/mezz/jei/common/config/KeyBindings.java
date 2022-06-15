package mezz.jei.common.config;

import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.keys.IJeiKeyMapping;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.Translator;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class KeyBindings implements IKeyBindings {
	private final IJeiKeyMapping toggleOverlay;
	private final IJeiKeyMapping focusSearch;
	private final IJeiKeyMapping toggleCheatMode;
	private final IJeiKeyMapping toggleEditMode;

	private final IJeiKeyMapping toggleCheatModeConfigButton;

	private final IJeiKeyMapping recipeBack;
	private final IJeiKeyMapping previousCategory;
	private final IJeiKeyMapping nextCategory;
	private final IJeiKeyMapping previousRecipePage;
	private final IJeiKeyMapping nextRecipePage;

	private final IJeiKeyMapping previousPage;
	private final IJeiKeyMapping nextPage;

	private final IJeiKeyMapping bookmark;
	private final IJeiKeyMapping toggleBookmarkOverlay;

	private final List<IJeiKeyMapping> showRecipe;
	private final List<IJeiKeyMapping> showUses;

	private final List<IJeiKeyMapping> cheatOneItem;
	private final List<IJeiKeyMapping> cheatItemStack;

	private final IJeiKeyMapping toggleHideIngredient;
	private final IJeiKeyMapping toggleWildcardHideIngredient;

	private final IJeiKeyMapping hoveredClearSearchBar;
	private final IJeiKeyMapping previousSearch;
	private final IJeiKeyMapping nextSearch;

	private final IJeiKeyMapping copyRecipeId;

	private final IJeiKeyMapping closeRecipeGui;

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	private final IJeiKeyMapping escapeKey;
	private final IJeiKeyMapping leftClick;
	private final IJeiKeyMapping rightClick;
	private final List<IJeiKeyMapping> enterKey;

	public KeyBindings() {
		IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();

		IJeiKeyMapping showRecipe1;
		IJeiKeyMapping showRecipe2;
		IJeiKeyMapping showUses1;
		IJeiKeyMapping showUses2;
		IJeiKeyMapping cheatOneItem1;
		IJeiKeyMapping cheatOneItem2;
		IJeiKeyMapping cheatItemStack1;
		IJeiKeyMapping cheatItemStack2;

		String overlaysCategoryName = Translator.translateToLocal("jei.key.category.overlays");
		IJeiKeyMappingCategoryBuilder overlay = inputHelper.createKeyMappingCategoryBuilder(overlaysCategoryName);

		String mouseHoverCategoryName = Translator.translateToLocal("jei.key.category.mouse.hover");
		IJeiKeyMappingCategoryBuilder mouseHover = inputHelper.createKeyMappingCategoryBuilder(mouseHoverCategoryName);

		String searchCategoryName = Translator.translateToLocal("jei.key.category.search");
		IJeiKeyMappingCategoryBuilder search = inputHelper.createKeyMappingCategoryBuilder(searchCategoryName);

		String cheatModeCategoryName = Translator.translateToLocal("jei.key.category.cheat.mode");
		IJeiKeyMappingCategoryBuilder cheat = inputHelper.createKeyMappingCategoryBuilder(cheatModeCategoryName);

		String hoverConfigButtonCategoryName = Translator.translateToLocal("jei.key.category.hover.config.button");
		IJeiKeyMappingCategoryBuilder hoverConfig = inputHelper.createKeyMappingCategoryBuilder(hoverConfigButtonCategoryName);

		String editModeCategoryName = Translator.translateToLocal("jei.key.category.edit.mode");
		IJeiKeyMappingCategoryBuilder editMode = inputHelper.createKeyMappingCategoryBuilder(editModeCategoryName);

		String recipeCategoryName = Translator.translateToLocal("jei.key.category.recipe.gui");
		IJeiKeyMappingCategoryBuilder recipeCategory = inputHelper.createKeyMappingCategoryBuilder(recipeCategoryName);

		String devToolsCategoryName = Translator.translateToLocal("jei.key.category.dev.tools");
		IJeiKeyMappingCategoryBuilder devTools = inputHelper.createKeyMappingCategoryBuilder(devToolsCategoryName);

		// Overlay
		toggleOverlay = overlay.createMapping("key.jei.toggleOverlay")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildKeyboardKey(GLFW.GLFW_KEY_O)
			.register();

		focusSearch = overlay.createMapping("key.jei.focusSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildKeyboardKey(GLFW.GLFW_KEY_F)
			.register();

		previousPage = overlay.createMapping("key.jei.previousPage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		nextPage = overlay.createMapping("key.jei.nextPage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		toggleBookmarkOverlay = overlay.createMapping("key.jei.toggleBookmarkOverlay")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		// Mouse Hover
		bookmark = mouseHover.createMapping("key.jei.bookmark")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_A)
			.register();

		showRecipe1 = mouseHover.createMapping("key.jei.showRecipe")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_R)
			.register();

		showRecipe2 = mouseHover.createMapping("key.jei.showRecipe2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildMouseLeft()
			.register();

		showUses1 = mouseHover.createMapping("key.jei.showUses")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_U)
			.register();

		showUses2 = mouseHover.createMapping("key.jei.showUses2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildMouseRight()
			.register();

		// Search Bar
		hoveredClearSearchBar = search.createMapping("key.jei.clearSearchBar")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_SEARCH)
			.buildMouseRight()
			.register();

		previousSearch = search.createMapping("key.jei.previousSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_UP)
			.register();

		nextSearch = search.createMapping("key.jei.nextSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_DOWN)
			.register();

		// Cheat Mode
		toggleCheatMode = cheat.createMapping("key.jei.toggleCheatMode")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		cheatOneItem1 = cheat.createMapping("key.jei.cheatOneItem")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseLeft()
			.register();

		cheatOneItem2 = cheat.createMapping("key.jei.cheatOneItem2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseRight()
			.register();

		cheatItemStack1 = cheat.createMapping("key.jei.cheatItemStack")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildMouseLeft()
			.register();

		cheatItemStack2 = cheat.createMapping("key.jei.cheatItemStack2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseMiddle()
			.register();

		// Hovering over config button
		toggleCheatModeConfigButton = hoverConfig.createMapping("key.jei.toggleCheatModeConfigButton")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CONFIG_BUTTON)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register();

		// Edit Mode
		toggleEditMode = editMode.createMapping("key.jei.toggleEditMode")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		toggleHideIngredient = editMode.createMapping("key.jei.toggleHideIngredient")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register();

		toggleWildcardHideIngredient = editMode.createMapping("key.jei.toggleWildcardHideIngredient")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseRight()
			.register();

		// Recipes
		recipeBack = recipeCategory.createMapping("key.jei.recipeBack")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_BACKSPACE)
			.register();

		previousRecipePage = recipeCategory.createMapping("key.jei.previousRecipePage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register();

		nextRecipePage = recipeCategory.createMapping("key.jei.nextRecipePage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register();

		previousCategory = recipeCategory.createMapping("key.jei.previousCategory")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register();

		nextCategory = recipeCategory.createMapping("key.jei.nextCategory")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register();

		closeRecipeGui = recipeCategory.createMapping("key.jei.closeRecipeGui")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_ESCAPE)
			.register();

		// Dev Tools
		copyRecipeId = devTools.createMapping("key.jei.copy.recipe.id")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register();

		showRecipe = List.of(showRecipe1, showRecipe2);
		showUses = List.of(showUses1, showUses2);
		cheatOneItem = List.of(cheatOneItem1, cheatOneItem2);
		cheatItemStack = List.of(cheatItemStack1, cheatItemStack2);

		String jeiHiddenInternalCategoryName = "jei.key.category.hidden.internal";
		IJeiKeyMappingCategoryBuilder jeiHidden = inputHelper.createKeyMappingCategoryBuilder(jeiHiddenInternalCategoryName);

		escapeKey = jeiHidden.createMapping("key.jei.internal.escape.key")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_ESCAPE);

		leftClick = jeiHidden.createMapping("key.jei.internal.left.click")
			.setContext(JeiKeyConflictContext.GUI)
			.buildMouseLeft();

		rightClick = jeiHidden.createMapping("key.jei.internal.right.click")
			.setContext(JeiKeyConflictContext.GUI)
			.buildMouseRight();

		enterKey = List.of(
			jeiHidden.createMapping("key.jei.internal.enter.key")
				.setContext(JeiKeyConflictContext.GUI)
				.buildKeyboardKey(GLFW.GLFW_KEY_ENTER),

			jeiHidden.createMapping("key.jei.internal.enter.key2")
				.setContext(JeiKeyConflictContext.GUI)
				.buildKeyboardKey(GLFW.GLFW_KEY_KP_ENTER)
		);
	}

	@Override
	public IJeiKeyMapping getToggleOverlay() {
		return toggleOverlay;
	}

	@Override
	public IJeiKeyMapping getFocusSearch() {
		return focusSearch;
	}

	@Override
	public IJeiKeyMapping getToggleCheatMode() {
		return toggleCheatMode;
	}

	@Override
	public IJeiKeyMapping getToggleEditMode() {
		return toggleEditMode;
	}

	@Override
	public IJeiKeyMapping getToggleCheatModeConfigButton() {
		return toggleCheatModeConfigButton;
	}

	@Override
	public IJeiKeyMapping getRecipeBack() {
		return recipeBack;
	}

	@Override
	public IJeiKeyMapping getPreviousCategory() {
		return previousCategory;
	}

	@Override
	public IJeiKeyMapping getNextCategory() {
		return nextCategory;
	}

	@Override
	public IJeiKeyMapping getPreviousRecipePage() {
		return previousRecipePage;
	}

	@Override
	public IJeiKeyMapping getNextRecipePage() {
		return nextRecipePage;
	}

	@Override
	public IJeiKeyMapping getPreviousPage() {
		return previousPage;
	}

	@Override
	public IJeiKeyMapping getNextPage() {
		return nextPage;
	}

	@Override
	public IJeiKeyMapping getCloseRecipeGui() {
		return closeRecipeGui;
	}

	@Override
	public IJeiKeyMapping getBookmark() {
		return bookmark;
	}

	@Override
	public IJeiKeyMapping getToggleBookmarkOverlay() {
		return toggleBookmarkOverlay;
	}

	@Override
	public List<IJeiKeyMapping> getShowRecipe() {
		return showRecipe;
	}

	@Override
	public List<IJeiKeyMapping> getShowUses() {
		return showUses;
	}

	@Override
	public List<IJeiKeyMapping> getCheatOneItem() {
		return cheatOneItem;
	}

	@Override
	public List<IJeiKeyMapping> getCheatItemStack() {
		return cheatItemStack;
	}

	@Override
	public IJeiKeyMapping getToggleHideIngredient() {
		return toggleHideIngredient;
	}

	@Override
	public IJeiKeyMapping getToggleWildcardHideIngredient() {
		return toggleWildcardHideIngredient;
	}

	@Override
	public IJeiKeyMapping getHoveredClearSearchBar() {
		return hoveredClearSearchBar;
	}

	@Override
	public IJeiKeyMapping getPreviousSearch() {
		return previousSearch;
	}

	@Override
	public IJeiKeyMapping getNextSearch() {
		return nextSearch;
	}

	@Override
	public IJeiKeyMapping getCopyRecipeId() {
		return copyRecipeId;
	}

	@Override
	public IJeiKeyMapping getEscapeKey() {
		return escapeKey;
	}

	@Override
	public IJeiKeyMapping getLeftClick() {
		return leftClick;
	}

	@Override
	public IJeiKeyMapping getRightClick() {
		return rightClick;
	}

	@Override
	public List<IJeiKeyMapping> getEnterKey() {
		return enterKey;
	}
}
