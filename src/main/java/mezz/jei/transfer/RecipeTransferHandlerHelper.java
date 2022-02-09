package mezz.jei.transfer;

import java.util.Collection;
import java.util.Set;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.util.ErrorUtil;
import net.minecraft.network.chat.Component;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(Component tooltipMessage) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	public IRecipeTransferError createUserErrorForMissingSlots(Component tooltipMessage, Collection<IRecipeSlotView> missingItemSlots) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

		return new RecipeTransferErrorMissingSlots(tooltipMessage, missingItemSlots);
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public IRecipeTransferError createUserErrorForSlots(Component tooltipMessage, Collection<Integer> missingItemIndexes) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemIndexes, "missingItemIndexes");

		return new RecipeTransferErrorIngredientIndexes(tooltipMessage, Set.copyOf(missingItemIndexes));
	}
}
