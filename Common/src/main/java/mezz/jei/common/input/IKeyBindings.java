package mezz.jei.common.input;

import mezz.jei.common.input.keys.IJeiKeyMapping;

import java.util.List;

public interface IKeyBindings {
    IJeiKeyMapping getToggleOverlay();
    IJeiKeyMapping getFocusSearch();
    IJeiKeyMapping getToggleCheatMode();
    IJeiKeyMapping getToggleEditMode();

    IJeiKeyMapping getToggleCheatModeConfigButton();

    IJeiKeyMapping getRecipeBack();
    IJeiKeyMapping getPreviousCategory();
    IJeiKeyMapping getNextCategory();
    IJeiKeyMapping getPreviousRecipePage();
    IJeiKeyMapping getNextRecipePage();

    IJeiKeyMapping getPreviousPage();
    IJeiKeyMapping getNextPage();

    IJeiKeyMapping getCloseRecipeGui();

    IJeiKeyMapping getBookmark();
    IJeiKeyMapping getToggleBookmarkOverlay();

    List<IJeiKeyMapping> getShowRecipe();
    List<IJeiKeyMapping> getShowUses();

    List<IJeiKeyMapping> getCheatOneItem();
    List<IJeiKeyMapping> getCheatItemStack();

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
    List<IJeiKeyMapping> getEnterKey();
}
