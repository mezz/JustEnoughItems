package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.Collection;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(@Nullable String tooltipMessage) {
		Preconditions.checkNotNull(tooltipMessage, "tooltipMessage cannot be null");

		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	public IRecipeTransferError createUserErrorForSlots(@Nullable String tooltipMessage, @Nullable Collection<Integer> missingItemSlots) {
		Preconditions.checkNotNull(tooltipMessage, "tooltipMessage cannot be null");
		Preconditions.checkNotNull(missingItemSlots, "missingItemSlots cannot be null");
		Preconditions.checkArgument(!missingItemSlots.isEmpty(), "middingItemSlots cannot be empty");

		return new RecipeTransferErrorSlots(tooltipMessage, missingItemSlots);
	}
}
