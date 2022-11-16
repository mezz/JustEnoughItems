package mezz.jei.api.recipe.transfer;

import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

/**
 * @since 11.5.0
 */
public interface IRecipeTransferManager {
    <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory);

}
