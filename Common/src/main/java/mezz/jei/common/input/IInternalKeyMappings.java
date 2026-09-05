package mezz.jei.common.input;

import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingWithExtraModifiers;

public interface IInternalKeyMappings extends IJeiKeyMappings {
	IJeiKeyMapping getToggleOverlay();
	IJeiKeyMapping getFocusSearch();
	IJeiKeyMapping getToggleCheatMode();
	IJeiKeyMapping getToggleEditMode();

	IJeiKeyMapping getToggleCheatModeConfigButton();

	IJeiKeyMapping getRecipeBack();
	IJeiKeyMapping getRecipeForward();
	IJeiKeyMapping getPreviousCategory();
	IJeiKeyMapping getNextCategory();
	IJeiKeyMapping getPreviousRecipePage();
	IJeiKeyMapping getNextRecipePage();
	IJeiKeyMappingInternal getPauseRecipeCycling();

	IJeiKeyMapping getPreviousPage();
	IJeiKeyMapping getNextPage();

	IJeiKeyMapping getCloseRecipeGui();

	@Override
	IJeiKeyMappingWithExtraModifiers getBookmark();
	IJeiKeyMapping getToggleBookmarkOverlay();

	@Override
	IJeiKeyMappingWithExtraModifiers getShowRecipe();

	@Override
	IJeiKeyMappingWithExtraModifiers getShowUses();

	IJeiKeyMapping getTransferRecipeBookmark();
	IJeiKeyMapping getMaxTransferRecipeBookmark();
	IJeiKeyMapping getQuickMove();
	IJeiKeyMapping getShareToChat();

	IJeiKeyMapping getCheatOneItem();
	IJeiKeyMapping getCheatItemStack();

	IJeiKeyMapping getToggleHideIngredient();
	IJeiKeyMapping getToggleWildcardHideIngredient();

	IJeiKeyMapping getHoveredClearSearchBar();
	IJeiKeyMapping getPreviousSearch();
	IJeiKeyMapping getNextSearch();

	IJeiKeyMapping getCopyRecipeId();

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	IJeiKeyMapping getEscapeKey();
	IJeiKeyMapping getLeftClick();
	IJeiKeyMapping getRightClick();
	IJeiKeyMapping getEnterKey();
}
