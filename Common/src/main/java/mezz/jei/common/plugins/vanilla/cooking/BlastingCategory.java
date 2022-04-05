package mezz.jei.common.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.helpers.IGuiHelper;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
	public BlastingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.BLAST_FURNACE, "gui.jei.category.blasting", 100);
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getUid() {
		return getRecipeType().getUid();
	}

	@SuppressWarnings("removal")
	@Override
	public Class<? extends BlastingRecipe> getRecipeClass() {
		return getRecipeType().getRecipeClass();
	}

	@Override
	public RecipeType<BlastingRecipe> getRecipeType() {
		return RecipeTypes.BLASTING;
	}
}
