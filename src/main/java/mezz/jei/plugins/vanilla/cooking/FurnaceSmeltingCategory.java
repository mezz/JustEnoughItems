package mezz.jei.plugins.vanilla.cooking;

import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<FurnaceRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.FURNACE, "gui.jei.category.smelting", 200);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.FURNACE;
	}

	@Override
	public Class<? extends FurnaceRecipe> getRecipeClass() {
		return FurnaceRecipe.class;
	}
}