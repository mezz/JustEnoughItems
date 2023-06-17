package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IGlobalRecipeCategoryExtension;

/**
 * The IAdvancedRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvanced(IAdvancedRegistration)}.
 */
public interface IAdvancedRegistration {

    /**
     * {@link IJeiHelpers} provides helpers and tools for addon mods.
     */
    IJeiHelpers getJeiHelpers();

    /**
     * Register your own {@link IRecipeManagerPlugin} here.
     */
    void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin);

    <T> void addGlobalRecipeCategoryExtension(IRecipeCategory<T> recipeCategory, IGlobalRecipeCategoryExtension<T> extension);

    <T> void addGlobalRecipeCategoryExtension(Class<T> recipeClass, IGlobalRecipeCategoryExtension<T> extension);

    <T> void addGlobalRecipeCategoryExtension(RecipeType<T> recipeType, IGlobalRecipeCategoryExtension<T> extension);
}
