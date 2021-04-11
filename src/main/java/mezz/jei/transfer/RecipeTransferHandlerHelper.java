package mezz.jei.transfer;

import java.util.Collection;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.util.ErrorUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	@Deprecated
	public IRecipeTransferError createUserErrorWithTooltip(String tooltipMessage) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

		return new RecipeTransferErrorTooltip(new StringTextComponent(tooltipMessage));
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(ITextComponent tooltipMessage) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	@Deprecated
	public IRecipeTransferError createUserErrorForSlots(String tooltipMessage, Collection<Integer> missingItemSlots) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

		return new RecipeTransferErrorSlots(new StringTextComponent(tooltipMessage), missingItemSlots);
	}

	@Override
	public IRecipeTransferError createUserErrorForSlots(ITextComponent tooltipMessage, Collection<Integer> missingItemSlots) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

		return new RecipeTransferErrorSlots(tooltipMessage, missingItemSlots);
	}
}
