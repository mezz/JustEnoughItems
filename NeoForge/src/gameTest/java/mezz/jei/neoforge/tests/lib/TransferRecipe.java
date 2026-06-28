package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

import java.util.ArrayList;
import java.util.List;

public record TransferRecipe<R>(R recipe, List<TestRecipeSlotView> inputSlots) {
	public IRecipeSlotsView slotsView() {
		return () -> new ArrayList<>(inputSlots);
	}
}
