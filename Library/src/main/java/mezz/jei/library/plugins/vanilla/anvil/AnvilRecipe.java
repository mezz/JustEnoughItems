package mezz.jei.library.plugins.vanilla.anvil;

import java.util.List;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record AnvilRecipe(
	List<ItemStack> leftInputs,
	List<ItemStack> rightInputs,
	List<ItemStack> outputs,
	ResourceLocation uid
) implements IJeiAnvilRecipe {

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

	@Override
	public ResourceLocation getUid() {
		return uid;
	}
}
