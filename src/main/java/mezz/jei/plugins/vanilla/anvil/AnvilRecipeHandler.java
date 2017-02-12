package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class AnvilRecipeHandler implements IRecipeHandler<AnvilRecipeWrapper>
{
    @Override
    public
    @Nonnull
    Class<AnvilRecipeWrapper> getRecipeClass()
    {
        return AnvilRecipeWrapper.class;
    }

    @Override
    public
    @Nonnull
    String getRecipeCategoryUid(@Nonnull AnvilRecipeWrapper recipe)
    {
        return VanillaRecipeCategoryUid.ANVIL;
    }

    @Override
    public
    @Nonnull
    IRecipeWrapper getRecipeWrapper(@Nonnull AnvilRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull AnvilRecipeWrapper recipe)
    {
        return true;
    }
}
