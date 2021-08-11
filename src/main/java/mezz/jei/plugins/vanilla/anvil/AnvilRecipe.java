package mezz.jei.plugins.vanilla.anvil;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.ImmutableList;

public class AnvilRecipe implements IJeiAnvilRecipe {
	private final List<List<ItemStack>> inputs;
	private final List<List<ItemStack>> outputs;

	public AnvilRecipe(List<ItemStack> leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		this.inputs = ImmutableList.of(leftInput, rightInputs);
		this.outputs = Collections.singletonList(outputs);
	}

	public List<List<ItemStack>> getInputs() {
		return inputs;
	}

	public List<List<ItemStack>> getOutputs() {
		return outputs;
	}
}
