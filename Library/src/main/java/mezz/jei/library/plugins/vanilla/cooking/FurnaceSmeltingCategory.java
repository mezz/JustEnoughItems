package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import mezz.jei.api.helpers.IGuiHelper;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.FURNACE, "gui.jei.category.smelting", 200);
	}

	@Override
	public RecipeType<SmeltingRecipe> getRecipeType() {
		return RecipeTypes.SMELTING;
	}
}
