package mezz.jei.plugins.vanilla.cooking;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.FURNACE, "gui.jei.category.smelting", 200);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.FURNACE;
	}

	@Override
	public Class<? extends SmeltingRecipe> getRecipeClass() {
		return SmeltingRecipe.class;
	}
}
