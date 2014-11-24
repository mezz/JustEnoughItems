package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.type.EnumRecipeType;
import mezz.jei.api.recipe.IRecipeGuiHelper;
import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class ShapedOreRecipeHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapedRecipeGui();
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

}
