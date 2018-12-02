package mezz.jei.plugins.vanilla;

import java.util.List;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {
	@Override
	public IRecipeWrapper createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");
		Preconditions.checkArgument(rightInputs.size() == outputs.size(), "Input and output sizes must match.");

		return new AnvilRecipeWrapper(leftInputs, rightInputs, outputs);
	}

	@Override
	public IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		ErrorUtil.checkNotEmpty(inputs, "inputs");
		ErrorUtil.checkNotEmpty(output, "output");

		return new SmeltingRecipe(inputs, output);
	}

	@Override
	public IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		return new BrewingRecipeWrapper(ingredients, potionInput, potionOutput);
	}
}
