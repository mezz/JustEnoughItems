package mezz.jei.recipe.crafting;

import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ShapelessOreRecipeWrapper implements ICraftingRecipeWrapper {

	@Nonnull
	private final ShapelessOreRecipe recipe;

	public ShapelessOreRecipeWrapper(@Nonnull Object recipe) {
		this.recipe = (ShapelessOreRecipe)recipe;
	}

	@Nonnull
	@Override
	public List getInputs() {
		return recipe.getInput();
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {

	}

}
