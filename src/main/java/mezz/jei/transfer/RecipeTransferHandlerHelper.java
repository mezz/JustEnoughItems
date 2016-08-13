package mezz.jei.transfer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(@Nullable String tooltipMessage) {
		if (tooltipMessage == null) {
			Log.error("Null tooltipMessage", new NullPointerException());
			tooltipMessage = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.unknown");
		}
		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	public IRecipeTransferError createUserErrorForSlots(@Nullable String tooltipMessage, @Nullable Collection<Integer> missingItemSlots) {
		if (tooltipMessage == null) {
			Log.error("Null tooltipMessage", new NullPointerException());
			tooltipMessage = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.unknown");
		}
		if (missingItemSlots == null) {
			Log.error("Null missingItemSlots", new NullPointerException());
			missingItemSlots = Collections.emptyList();
		} else if (missingItemSlots.isEmpty()) {
			Log.error("Empty missingItemSlots", new IllegalArgumentException());
		}

		return new RecipeTransferErrorSlots(tooltipMessage, missingItemSlots);
	}
}
