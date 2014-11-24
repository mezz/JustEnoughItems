package mezz.jei.plugins.forestry.crafting;

import forestry.core.interfaces.IDescriptiveRecipe;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ForestryShapedRecipeWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final IDescriptiveRecipe recipe;

	public ForestryShapedRecipeWrapper(@Nonnull Object recipe) {
		this.recipe = (IDescriptiveRecipe)recipe;
	}

	@Override
	public List getInputs() {
		return Arrays.asList(recipe.getIngredients());
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {

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
