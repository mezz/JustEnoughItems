package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class FurnaceFuelCategory extends FurnaceRecipeCategory {

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public void init(@Nonnull IRecipeLayout recipeLayout) {
		recipeLayout.setRecipeTransferButton(-14, 36);

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(inputSlot, false, 0, 0);
		guiItemStacks.init(fuelSlot, true, 0, 36);
		guiItemStacks.init(outputSlot, false, 60, 18);
	}
}
