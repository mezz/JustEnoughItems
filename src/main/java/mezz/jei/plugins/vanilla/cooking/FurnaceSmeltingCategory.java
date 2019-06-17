package mezz.jei.plugins.vanilla.cooking;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.util.ResourceLocation;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<FurnaceRecipe> {
    public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
        super(guiHelper, Blocks.FURNACE, "smelting", 200);
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