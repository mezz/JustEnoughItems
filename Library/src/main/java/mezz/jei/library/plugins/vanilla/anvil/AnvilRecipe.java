package mezz.jei.library.plugins.vanilla.anvil;

import java.util.List;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.world.item.ItemStack;

public class AnvilRecipe implements IJeiAnvilRecipe {
	private final List<ItemStack> leftInputs;
	private final List<ItemStack> rightInputs;
	private final List<ItemStack> outputs;

	public AnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		this.leftInputs = List.copyOf(leftInputs);
		this.rightInputs = List.copyOf(rightInputs);
		this.outputs = List.copyOf(outputs);
	}

	@Override
	public List<ItemStack> getLeftInputs() {
		return leftInputs;
	}

	@Override
	public List<ItemStack> getRightInputs() {
		return rightInputs;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return outputs;
	}
}
