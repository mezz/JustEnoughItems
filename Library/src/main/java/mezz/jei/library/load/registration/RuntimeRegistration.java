package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.library.gui.BookmarkOverlayDummy;
import mezz.jei.library.gui.IngredientListOverlayDummy;
import mezz.jei.library.gui.recipes.RecipesGuiDummy;
import mezz.jei.library.ingredients.IngredientFilterApiDummy;

public class RuntimeRegistration implements IRuntimeRegistration {
    private final IRecipeManager recipeManager;
    private final IJeiHelpers jeiHelpers;
    private final IEditModeConfig editModeConfig;
    private final IIngredientManager ingredientManager;
    private final IIngredientVisibility ingredientVisibility;
    private final IRecipeTransferManager recipeTransferManager;
    private final IScreenHelper screenHelper;

    private IIngredientListOverlay ingredientListOverlay = IngredientListOverlayDummy.INSTANCE;
    private IBookmarkOverlay bookmarkOverlay = BookmarkOverlayDummy.INSTANCE;
    private IRecipesGui recipesGui = RecipesGuiDummy.INSTANCE;
    private IIngredientFilter ingredientFilter = IngredientFilterApiDummy.INSTANCE;

    public RuntimeRegistration(
        IRecipeManager recipeManager,
        IJeiHelpers jeiHelpers,
        IEditModeConfig editModeConfig,
        IIngredientManager ingredientManager,
        IIngredientVisibility ingredientVisibility,
        IRecipeTransferManager recipeTransferManager,
        IScreenHelper screenHelper
    ) {
        this.recipeManager = recipeManager;
        this.jeiHelpers = jeiHelpers;
        this.editModeConfig = editModeConfig;
        this.ingredientManager = ingredientManager;
        this.ingredientVisibility = ingredientVisibility;
        this.recipeTransferManager = recipeTransferManager;
        this.screenHelper = screenHelper;
    }

    @Override
    public void setIngredientListOverlay(IIngredientListOverlay ingredientListOverlay) {
        this.ingredientListOverlay = ingredientListOverlay;
    }

    @Override
    public void setBookmarkOverlay(IBookmarkOverlay bookmarkOverlay) {
        this.bookmarkOverlay = bookmarkOverlay;
    }

    @Override
    public void setRecipesGui(IRecipesGui recipesGui) {
        this.recipesGui = recipesGui;
    }

    @Override
    public void setIngredientFilter(IIngredientFilter ingredientFilter) {
        this.ingredientFilter = ingredientFilter;
    }

    @Override
    public IRecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return this.jeiHelpers;
    }

    @Override
    public IIngredientManager getIngredientManager() {
        return this.ingredientManager;
    }

    @Override
    public IIngredientVisibility getIngredientVisibility() {
        return this.ingredientVisibility;
    }

    @Override
    public IScreenHelper getScreenHelper() {
        return this.screenHelper;
    }

    @Override
    public IRecipeTransferManager getRecipeTransferManager() {
        return this.recipeTransferManager;
    }

    @Override
    public IEditModeConfig getEditModeConfig() {
        return this.editModeConfig;
    }

    public IIngredientListOverlay getIngredientListOverlay() {
        return ingredientListOverlay;
    }

    public IBookmarkOverlay getBookmarkOverlay() {
        return bookmarkOverlay;
    }

    public IRecipesGui getRecipesGui() {
        return recipesGui;
    }

    public IIngredientFilter getIngredientFilter() {
        return this.ingredientFilter;
    }
}
