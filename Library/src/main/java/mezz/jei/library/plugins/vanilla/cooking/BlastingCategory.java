package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
	public BlastingCategory(IGuiHelper guiHelper, List<ItemStack> furnaceFuels) {
		super(guiHelper, RecipeTypes.BLASTING, Blocks.BLAST_FURNACE, "gui.jei.category.blasting", 100, furnaceFuels);
	}
}
