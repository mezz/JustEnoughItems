package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.CraftingRecipe;

/**
 * Allows extending the vanilla crafting recipe category,
 * to support custom recipes classes that cannot be handled by default.
 *
 * Get the instance from {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 *
 * @since 16.0.0
 */
public interface IExtendableCraftingRecipeCategory {
    /**
     * Add an extension that handles a subset of the recipes in the recipe category.
     *
     * @param recipeClass  the subset class of crafting recipes to handle
     * @param extension    an extension for handling these recipes
     * @since 16.0.0
     */
    <R extends CraftingRecipe> void addExtension(Class<? extends R> recipeClass, ICraftingCategoryExtension<R> extension);
}
