package mezz.jei.gui.recipes.builder;

import mezz.jei.api.gui.builder.IRecipeSlotId;

public record RecipeSlotId(int internalId) implements IRecipeSlotId {
	private static int nextId = 0;

	public static RecipeSlotId create() {
		return new RecipeSlotId(nextId++);
	}
}
