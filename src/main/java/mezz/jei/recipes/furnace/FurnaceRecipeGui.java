package mezz.jei.recipes.furnace;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeGuiHelper;

import javax.annotation.Nonnull;

public abstract class FurnaceRecipeGui implements IRecipeGuiHelper {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	@Override
	public void initGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks) {
		guiItemStacks.initItemStack(inputSlot, 0, 0);
		guiItemStacks.initItemStack(fuelSlot, 0, 36);
		guiItemStacks.initItemStack(outputSlot, 60, 18);
	}

}
