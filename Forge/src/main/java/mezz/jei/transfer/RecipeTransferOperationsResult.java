package mezz.jei.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import java.util.ArrayList;
import java.util.List;

public class RecipeTransferOperationsResult {
	/**
	 * map of "recipe target slot" to "source inventory slot"
	 */
	public final List<TransferOperation> results = new ArrayList<>();
	/**
	 * array of missing "required item stacks"
	 */
	public final List<IRecipeSlotView> missingItems = new ArrayList<>();
}
