package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedOreRecipeWrapper extends ShapelessRecipeWrapper<ShapedOreRecipe> implements IShapedCraftingRecipeWrapper {
	public ShapedOreRecipeWrapper(IJeiHelpers jeiHelpers, ShapedOreRecipe recipe) {
		super(jeiHelpers, recipe);
	}

	@Override
	public int getWidth() {
		return recipe.getWidth();
	}

	@Override
	public int getHeight() {
		return recipe.getHeight();
	}

}
