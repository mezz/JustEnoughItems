package mezz.jei.plugins.vanilla.cooking;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
	public BlastingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.BLAST_FURNACE, "gui.jei.category.blasting", 100);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.BLASTING;
	}

	@Override
	public Class<? extends BlastingRecipe> getRecipeClass() {
		return BlastingRecipe.class;
	}
}
