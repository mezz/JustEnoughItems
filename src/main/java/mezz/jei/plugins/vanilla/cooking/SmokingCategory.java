package mezz.jei.plugins.vanilla.cooking;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.util.ResourceLocation;

public class SmokingCategory extends AbstractCookingCategory<SmokingRecipe> {
    public SmokingCategory(IGuiHelper guiHelper) {
        super(guiHelper, Blocks.SMOKER, "smoking", 100);
    }

    @Override
    public ResourceLocation getUid() {
        return VanillaRecipeCategoryUid.SMOKING;
    }

    @Override
    public Class<? extends SmokingRecipe> getRecipeClass() {
        return SmokingRecipe.class;
    }
}