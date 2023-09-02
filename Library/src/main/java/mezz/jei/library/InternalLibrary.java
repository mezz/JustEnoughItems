package mezz.jei.library;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;

public final class InternalLibrary {

    private static IIngredientManager ingredientManager;
    private static IRecipeManager recipeManager;
    private static IFocusFactory focusFactory;

    public static IIngredientManager getIngredientManager() {
        Preconditions.checkState(ingredientManager != null, "Ingredient Manager has not been created yet.");
        return ingredientManager;
    }

    public static IRecipeManager getRecipeManager() {
        Preconditions.checkState(recipeManager != null, "RecipeManager has not been created yet.");
        return recipeManager;
    }

    public static IFocusFactory getFocusFactory() {
        Preconditions.checkState(focusFactory != null, "Focus Factory have not been created yet.");
        return focusFactory;
    }

    public static void setIngredientManager(IIngredientManager ingredientManager) {
        InternalLibrary.ingredientManager = ingredientManager;
    }

    public static void setRecipeManager(IRecipeManager recipeManager) {
        InternalLibrary.recipeManager = recipeManager;
    }

    public static void setFocusFactory(IFocusFactory focusFactory) {
        InternalLibrary.focusFactory = focusFactory;
    }
}
