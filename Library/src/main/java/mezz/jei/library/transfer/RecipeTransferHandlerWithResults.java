package mezz.jei.library.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.transfer.PendingRecipeTransfers;
import mezz.jei.common.transfer.RecipeTransferContextWithCompletion;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class RecipeTransferHandlerWithResults<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private final IRecipeTransferHandler<C, R> handler;

	private RecipeTransferHandlerWithResults(IRecipeTransferHandler<C, R> handler) {
		this.handler = handler;
	}

	public static <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> create(
		IRecipeTransferHandler<C, R> handler
	) {
		ErrorUtil.checkNotNull(handler, "handler");
		return new RecipeTransferHandlerWithResults<>(handler);
	}

	@Override
	public Class<? extends C> getContainerClass() {
		return handler.getContainerClass();
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return handler.getMenuType();
	}

	@Override
	public IRecipeType<R> getRecipeType() {
		return handler.getRecipeType();
	}

	@SuppressWarnings("removal")
	@Override
	public @Nullable IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		return handler.transferRecipe(container, recipe, recipeSlots, player, maxTransfer, doTransfer);
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer) {
		if (!doTransfer) {
			return handler.transferRecipe(new RecipeTransferContextWithCompletion<>(context, result -> {}), false);
		}
		PendingRecipeTransfers pendingTransfers = PendingRecipeTransfers.INSTANCE;
		IRecipeTransferContext<R, C> existing = pendingTransfers.getTrackedContext(context);
		if (existing != null) {
			return handler.transferRecipe(existing, true);
		}
		IRecipeTransferContext<R, C> trackedContext = pendingTransfers.registerRecipeTransfer(context, handler);
		boolean awaitingResult = false;
		try {
			IRecipeTransferError error = handler.transferRecipe(trackedContext, true);
			awaitingResult = error == null || error.getType().allowsTransfer;
			return error;
		} finally {
			if (!awaitingResult) {
				pendingTransfers.discard(context.getTransferId(), handler);
			}
		}
	}
}
