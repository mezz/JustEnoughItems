package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ShapelessOreRecipeWrapper implements IRecipeWrapper {

	@Nonnull
	private final ShapelessOreRecipe recipe;

	public ShapelessOreRecipeWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapelessOreRecipe)recipe;
	}

	@Override
	public List getInputs() {
		return recipe.getInput();
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}

}
