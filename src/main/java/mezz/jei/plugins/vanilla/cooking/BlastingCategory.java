package mezz.jei.plugins.vanilla.cooking;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.util.ResourceLocation;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
    public BlastingCategory(IGuiHelper guiHelper) {
        super(guiHelper, Blocks.BLAST_FURNACE, "blasting", 100);
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