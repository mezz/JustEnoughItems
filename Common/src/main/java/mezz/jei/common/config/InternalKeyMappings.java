package mezz.jei.common.config;

import java.util.function.Consumer;

import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.keys.IJeiKeyMappingCategoryBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.common.input.keys.JeiMultiKeyMapping;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.Translator;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class InternalKeyMappings implements IInternalKeyMappings {
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

	private final IJeiKeyMapping showRecipe;
	private final IJeiKeyMapping showUses;

	private final IJeiKeyMapping cheatOneItem;
	private final IJeiKeyMapping cheatItemStack;

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
	private final IJeiKeyMapping enterKey;

	public InternalKeyMappings(Consumer<KeyMapping> registerMethod) {
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
			.register(registerMethod);

		focusSearch = overlay.createMapping("key.jei.focusSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildKeyboardKey(GLFW.GLFW_KEY_F)
			.register(registerMethod);

		previousPage = overlay.createMapping("key.jei.previousPage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		nextPage = overlay.createMapping("key.jei.nextPage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		toggleBookmarkOverlay = overlay.createMapping("key.jei.toggleBookmarkOverlay")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		// Mouse Hover
		bookmark = mouseHover.createMapping("key.jei.bookmark")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_A)
			.register(registerMethod);

		showRecipe1 = mouseHover.createMapping("key.jei.showRecipe")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_R)
			.register(registerMethod);

		showRecipe2 = mouseHover.createMapping("key.jei.showRecipe2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildMouseLeft()
			.register(registerMethod);

		showUses1 = mouseHover.createMapping("key.jei.showUses")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_U)
			.register(registerMethod);

		showUses2 = mouseHover.createMapping("key.jei.showUses2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.buildMouseRight()
			.register(registerMethod);

		// Search Bar
		hoveredClearSearchBar = search.createMapping("key.jei.clearSearchBar")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_SEARCH)
			.buildMouseRight()
			.register(registerMethod);

		previousSearch = search.createMapping("key.jei.previousSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_UP)
			.register(registerMethod);

		nextSearch = search.createMapping("key.jei.nextSearch")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_DOWN)
			.register(registerMethod);

		// Cheat Mode
		toggleCheatMode = cheat.createMapping("key.jei.toggleCheatMode")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		cheatOneItem1 = cheat.createMapping("key.jei.cheatOneItem")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseLeft()
			.register(registerMethod);

		cheatOneItem2 = cheat.createMapping("key.jei.cheatOneItem2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseRight()
			.register(registerMethod);

		cheatItemStack1 = cheat.createMapping("key.jei.cheatItemStack")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildMouseLeft()
			.register(registerMethod);

		cheatItemStack2 = cheat.createMapping("key.jei.cheatItemStack2")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CHEAT_MODE)
			.buildMouseMiddle()
			.register(registerMethod);

		// Hovering over config button
		toggleCheatModeConfigButton = hoverConfig.createMapping("key.jei.toggleCheatModeConfigButton")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER_CONFIG_BUTTON)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register(registerMethod);

		// Edit Mode
		toggleEditMode = editMode.createMapping("key.jei.toggleEditMode")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		toggleHideIngredient = editMode.createMapping("key.jei.toggleHideIngredient")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register(registerMethod);

		toggleWildcardHideIngredient = editMode.createMapping("key.jei.toggleWildcardHideIngredient")
			.setContext(JeiKeyConflictContext.JEI_GUI_HOVER)
			.setModifier(JeiKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseRight()
			.register(registerMethod);

		// Recipes
		recipeBack = recipeCategory.createMapping("key.jei.recipeBack")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_BACKSPACE)
			.register(registerMethod);

		previousRecipePage = recipeCategory.createMapping("key.jei.previousRecipePage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register(registerMethod);

		nextRecipePage = recipeCategory.createMapping("key.jei.nextRecipePage")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register(registerMethod);

		previousCategory = recipeCategory.createMapping("key.jei.previousCategory")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register(registerMethod);

		nextCategory = recipeCategory.createMapping("key.jei.nextCategory")
			.setContext(JeiKeyConflictContext.GUI)
			.setModifier(JeiKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register(registerMethod);

		closeRecipeGui = recipeCategory.createMapping("key.jei.closeRecipeGui")
			.setContext(JeiKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_ESCAPE)
			.register(registerMethod);

		// Dev Tools
		copyRecipeId = devTools.createMapping("key.jei.copy.recipe.id")
			.setContext(JeiKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		showRecipe = new JeiMultiKeyMapping(showRecipe1, showRecipe2);
		showUses = new JeiMultiKeyMapping(showUses1, showUses2);
		cheatOneItem = new JeiMultiKeyMapping(cheatOneItem1, cheatOneItem2);
		cheatItemStack = new JeiMultiKeyMapping(cheatItemStack1, cheatItemStack2);

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

		enterKey = new JeiMultiKeyMapping(
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
	public IJeiKeyMapping getShowRecipe() {
		return showRecipe;
	}

	@Override
	public IJeiKeyMapping getShowUses() {
		return showUses;
	}

	@Override
	public IJeiKeyMapping getCheatOneItem() {
		return cheatOneItem;
	}

	@Override
	public IJeiKeyMapping getCheatItemStack() {
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
	public IJeiKeyMapping getEnterKey() {
		return enterKey;
	}
}
