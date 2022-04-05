package mezz.jei.common.input;

import net.minecraft.client.KeyMapping;

import java.util.List;

public interface IKeyBindings {
    KeyMapping getToggleOverlay();
    KeyMapping getFocusSearch();
    KeyMapping getToggleCheatMode();
    KeyMapping getToggleEditMode();

    KeyMapping getToggleCheatModeConfigButton();

    KeyMapping getRecipeBack();
    KeyMapping getPreviousCategory();
    KeyMapping getNextCategory();
    KeyMapping getPreviousRecipePage();
    KeyMapping getNextRecipePage();

    KeyMapping getPreviousPage();
    KeyMapping getNextPage();

    KeyMapping getBookmark();
    KeyMapping getToggleBookmarkOverlay();

    List<KeyMapping> getShowRecipe();
    List<KeyMapping> getShowUses();

    List<KeyMapping> getCheatOneItem();
    List<KeyMapping> getCheatItemStack();

    KeyMapping getToggleHideIngredient();
    KeyMapping getToggleWildcardHideIngredient();

    KeyMapping getHoveredClearSearchBar();
    KeyMapping getPreviousSearch();
    KeyMapping getNextSearch();

    KeyMapping getCopyRecipeId();

    // internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
    KeyMapping getEscapeKey();
    KeyMapping getLeftClick();
    KeyMapping getRightClick();
    List<KeyMapping> getEnterKey();

    // debug only
    KeyMapping getReloadJeiOverTextFilter();
}
