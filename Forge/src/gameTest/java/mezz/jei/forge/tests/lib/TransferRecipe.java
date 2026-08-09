package mezz.jei.forge.tests.lib;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import java.util.ArrayList;
import java.util.List;

public record TransferRecipe<R>(R recipe, List<TestRecipeSlotView> inputSlots) {
	public TestRecipeSlotsView slotsView() {
		List<IRecipeSlotView> slots = new ArrayList<>(inputSlots);
		return new TestRecipeSlotsView(slots);
	}
}
