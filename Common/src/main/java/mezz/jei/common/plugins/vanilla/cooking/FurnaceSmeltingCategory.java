package mezz.jei.common.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.helpers.IGuiHelper;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.FURNACE, "gui.jei.category.smelting", 200);
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getUid() {
		return getRecipeType().getUid();
	}

	@SuppressWarnings("removal")
	@Override
	public Class<? extends SmeltingRecipe> getRecipeClass() {
		return getRecipeType().getRecipeClass();
	}

	@Override
	public RecipeType<SmeltingRecipe> getRecipeType() {
		return RecipeTypes.SMELTING;
	}
}
