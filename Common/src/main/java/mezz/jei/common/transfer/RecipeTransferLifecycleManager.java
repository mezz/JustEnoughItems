package mezz.jei.common.transfer;

import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class RecipeTransferLifecycleManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static int nextTransferId = 1;
	private final List<IRecipeTransferListener> recipeTransferListeners;

	public RecipeTransferLifecycleManager(List<IRecipeTransferListener> recipeTransferListeners) {
		this.recipeTransferListeners = List.copyOf(recipeTransferListeners);
	}

	public int getNextTransferId() {
		int transferId = nextTransferId++;
		if (nextTransferId <= 0) {
			nextTransferId = 1;
		}
		return transferId;
	}

	public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
		ErrorUtil.checkNotNull(context, "context");
		for (IRecipeTransferListener listener : recipeTransferListeners) {
			try {
				listener.beforeRecipeTransfer(context);
			} catch (RuntimeException e) {
				LOGGER.error("Recipe transfer listener '{}' threw an error before recipe transfer: ", listener.getClass(), e);
			}
		}
	}

	public void completeRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
		ErrorUtil.checkNotNull(context, "context");
		ErrorUtil.checkNotNull(result, "result");
		for (IRecipeTransferListener listener : recipeTransferListeners) {
			try {
				listener.afterRecipeTransfer(context, result);
			} catch (RuntimeException e) {
				LOGGER.error("Recipe transfer listener '{}' threw an error after recipe transfer: ", listener.getClass(), e);
			}
		}
	}
}
