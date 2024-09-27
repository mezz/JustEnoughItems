package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.BlastingRecipe;

import mezz.jei.api.helpers.IGuiHelper;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
	public BlastingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.BLASTING, Blocks.BLAST_FURNACE, "gui.jei.category.blasting", 100);
	}
}
