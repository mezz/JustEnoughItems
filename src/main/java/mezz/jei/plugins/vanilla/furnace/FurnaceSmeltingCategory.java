package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory {
	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.SMELTING;
	}

	@Override
	public void init(@Nonnull IRecipeLayout recipeLayout) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(fuelSlot, false, 0, 36);
		guiItemStacks.init(outputSlot, false, 60, 18);
	}
}
