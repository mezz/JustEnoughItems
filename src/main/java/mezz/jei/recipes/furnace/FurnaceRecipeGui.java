package mezz.jei.recipes.furnace;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableRecipePng;
import mezz.jei.recipes.RecipeGui;

public abstract class FurnaceRecipeGui extends RecipeGui {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	public FurnaceRecipeGui() {
		super(new DrawableRecipePng(RecipeType.FURNACE));

		IGuiHelper guiHelper = JEIManager.guiHelper;

		addItem(guiHelper.makeGuiItemStack(0, 0, 1));
		addItem(guiHelper.makeGuiItemStack(0, 36, 1));
		addItem(guiHelper.makeGuiItemStack(60, 18, 1));
	}

}
