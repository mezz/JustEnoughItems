package mezz.jei.common.transfer;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public final class RecipeTransferService {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IRecipeTransferManager recipeTransferManager;
	private final RecipeTransferLifecycleManager recipeTransferLifecycleManager;

	public RecipeTransferService(IRecipeTransferManager recipeTransferManager) {
		ErrorUtil.checkNotNull(recipeTransferManager, "recipeTransferManager");
		this.recipeTransferManager = recipeTransferManager;
		this.recipeTransferLifecycleManager = new RecipeTransferLifecycleManager(recipeTransferManager.getRecipeTransferListeners());
	}

	public <C extends AbstractContainerMenu> boolean transferRecipe(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<?> recipeLayout,
		Player player,
		boolean maxTransfer
	) {
		return transferRecipe(screen, recipeLayout, player, maxTransfer, true)
			.map(error -> error.getType().allowsTransfer)
			.orElse(true);
	}

	public <C extends AbstractContainerMenu> Optional<IRecipeTransferError> getTransferRecipeError(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<?> recipeLayout,
		Player player
	) {
		return transferRecipe(screen, recipeLayout, player, false, false);
	}

	public <C extends AbstractContainerMenu, R> boolean hasRecipeTransferHandler(
		C container,
		IRecipeCategory<R> recipeCategory
	) {
		return recipeTransferManager.getRecipeTransferHandler(container, recipeCategory).isPresent();
	}

	private <C extends AbstractContainerMenu, R> Optional<IRecipeTransferError> transferRecipe(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		C container = screen.getMenu();
		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();

		Optional<IRecipeTransferHandler<C, R>> recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(container, recipeCategory);
		if (recipeTransferHandler.isEmpty()) {
			if (doTransfer) {
				LOGGER.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}

		IRecipeTransferHandler<C, R> transferHandler = recipeTransferHandler.get();
		IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
		RecipeTransferContext<R, C> context = new RecipeTransferContext<>(
			recipeTransferLifecycleManager.getNextTransferId(),
			recipeLayout.getRecipe(),
			recipeCategory.getRecipeType(),
			screen,
			recipeSlotsView,
			player,
			maxTransfer,
			recipeTransferLifecycleManager
		);
		if (doTransfer) {
			recipeTransferLifecycleManager.beforeRecipeTransfer(context);
		}

		try {
			IRecipeTransferError transferError = transferHandler.transferRecipe(context, doTransfer);
			return Optional.ofNullable(transferError);
		} catch (RuntimeException e) {
			LOGGER.error(
				"Recipe transfer handler '{}' for container '{}' and recipe type '{}' threw an error: ",
				transferHandler.getClass(), transferHandler.getContainerClass(), recipeCategory.getRecipeType(), e
			);
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}
	}
}
