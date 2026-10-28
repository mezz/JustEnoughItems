package mezz.jei.common.transfer;

import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class PendingRecipeTransfers {
	public static final PendingRecipeTransfers INSTANCE = new PendingRecipeTransfers();
	private final Map<Integer, PendingTransfer> pendingTransfers = new HashMap<>();

	private PendingRecipeTransfers() {
	}

	public <R, C extends AbstractContainerMenu> IRecipeTransferContext<R, C> registerRecipeTransfer(IRecipeTransferContext<R, C> context, Object owner) {
		ErrorUtil.checkNotNull(context, "context");
		int transferId = context.getTransferId();
		PendingTransfer transfer = pendingTransfers.computeIfAbsent(transferId, id -> new PendingTransfer(context, owner));
		return new RecipeTransferContextWithCompletion<>(context, transfer::complete);
	}

	public <R, C extends AbstractContainerMenu> @Nullable IRecipeTransferContext<R, C> getTrackedContext(IRecipeTransferContext<R, C> context) {
		PendingTransfer transfer = pendingTransfers.get(context.getTransferId());
		if (transfer == null) {
			return null;
		}
		return new RecipeTransferContextWithCompletion<>(context, transfer::complete);
	}

	public void completeRecipeTransfer(int transferId, RecipeTransferResult result) {
		ErrorUtil.checkNotNull(result, "result");
		PendingTransfer transfer = pendingTransfers.get(transferId);
		if (transfer != null) {
			transfer.complete(result);
		}
	}

	public void clear() {
		pendingTransfers.clear();
	}

	public void discard(int transferId, Object owner) {
		PendingTransfer transfer = pendingTransfers.get(transferId);
		if (transfer != null && transfer.owner == owner) {
			pendingTransfers.remove(transferId);
		}
	}

	private final class PendingTransfer {
		private final IRecipeTransferContext<?, ?> context;
		private final Object owner;

		private PendingTransfer(IRecipeTransferContext<?, ?> context, Object owner) {
			this.context = context;
			this.owner = owner;
		}

		private void complete(RecipeTransferResult result) {
			ErrorUtil.checkNotNull(result, "result");
			if (pendingTransfers.remove(context.getTransferId(), this)) {
				context.completeRecipeTransfer(result);
			}
		}
	}
}
