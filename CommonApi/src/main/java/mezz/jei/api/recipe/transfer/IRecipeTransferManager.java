package mezz.jei.api.recipe.transfer;

import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

/**
 * Holds all the registered recipe transfer handlers.
 *
 * @since 11.5.0
 */
public interface IRecipeTransferManager {
    /**
     * Get a recipe transfer handler for the given container and recipe category, if one is registered for it.
     *
     * @since 11.5.0
     */
    <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory);
}
