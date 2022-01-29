package mezz.jei.plugins.vanilla.anvil;

import java.util.List;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.world.item.ItemStack;

public class AnvilRecipe implements IJeiAnvilRecipe {
	private final List<ItemStack> leftInputs;
	private final List<ItemStack> rightInputs;
	private final List<ItemStack> outputs;

	public AnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		this.leftInputs = leftInputs;
		this.rightInputs = rightInputs;
		this.outputs = outputs;
	}

	public List<ItemStack> getLeftInputs() {
		return leftInputs;
	}

	public List<ItemStack> getRightInputs() {
		return rightInputs;
	}

	public List<ItemStack> getOutputs() {
		return outputs;
	}
}
