package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class AnvilRecipeHandler implements IRecipeHandler<AnvilRecipeWrapper> {

    @Override
    public Class<AnvilRecipeWrapper> getRecipeClass() {
        return AnvilRecipeWrapper.class;
    }

    @Override
    public String getRecipeCategoryUid(AnvilRecipeWrapper recipe) {
        return VanillaRecipeCategoryUid.ANVIL;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(AnvilRecipeWrapper recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(AnvilRecipeWrapper recipe) {
        return true;
    }
}
