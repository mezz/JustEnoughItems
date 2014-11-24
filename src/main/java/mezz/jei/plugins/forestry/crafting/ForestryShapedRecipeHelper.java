package mezz.jei.plugins.forestry.crafting;

import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.EnumRecipeTypeKey;
import mezz.jei.api.recipe.type.IRecipeTypeKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForestryShapedRecipeHelper implements IRecipeHelper {

	@Nullable
	@Override
	public Class getRecipeClass() {
		try {
			return Class.forName("forestry.core.utils.ShapedRecipeCustom");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public IRecipeTypeKey getRecipeTypeKey() {
		return EnumRecipeTypeKey.CRAFTING_TABLE;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Object recipe) {
		return new ForestryShapedRecipeWrapper(recipe);
	}
}
