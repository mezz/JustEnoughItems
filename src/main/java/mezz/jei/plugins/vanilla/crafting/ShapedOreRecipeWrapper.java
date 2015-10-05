package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class ShapedOreRecipeWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final ShapedOreRecipe recipe;
	private final int width;
	private final int height;

	public ShapedOreRecipeWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapedOreRecipe) recipe;
		this.width = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "width");
		this.height = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "height");
	}

	@Nonnull
	@Override
	public List getInputs() {
		return Arrays.asList(recipe.getInput());
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
