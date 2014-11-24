package mezz.jei.recipe.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ShapedRecipesWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final ShapedRecipes recipe;

	public ShapedRecipesWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapedRecipes)recipe;
	}

	@Override
	public List getInputs() {
		return Arrays.asList(recipe.recipeItems);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}

	@Override
	public int getWidth() {
		return recipe.recipeWidth;
	}

	@Override
	public int getHeight() {
		return recipe.recipeHeight;
	}

}
