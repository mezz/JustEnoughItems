package mezz.jei.common.transfer;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Optional;

public final class RecipeTransferService {
	private final IRecipeTransferManager recipeTransferManager;

	public RecipeTransferService(IRecipeTransferManager recipeTransferManager) {
		ErrorUtil.checkNotNull(recipeTransferManager, "recipeTransferManager");
		this.recipeTransferManager = recipeTransferManager;
	}

	public <C extends AbstractContainerMenu> boolean transferRecipe(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<?> recipeLayout,
		Player player,
		boolean maxTransfer
	) {
		return recipeTransferManager.transferRecipe(screen, recipeLayout, player, maxTransfer, true)
			.map(error -> error.getType().allowsTransfer)
			.orElse(true);
	}

	public <C extends AbstractContainerMenu> Optional<IRecipeTransferError> getTransferRecipeError(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<?> recipeLayout,
		Player player
	) {
		return recipeTransferManager.transferRecipe(screen, recipeLayout, player, false, false);
	}

	public <C extends AbstractContainerMenu, R> boolean hasRecipeTransferHandler(
		C container,
		IRecipeCategory<R> recipeCategory
	) {
		return recipeTransferManager.getRecipeTransferHandler(container, recipeCategory).isPresent();
	}
}
