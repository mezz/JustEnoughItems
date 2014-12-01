package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShapedRecipesWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final ShapedRecipes recipe;

	public ShapedRecipesWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapedRecipes)recipe;
	}

	@Nonnull
	@Override
	public List getInputs() {
		return Arrays.asList(recipe.recipeItems);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft) {

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
