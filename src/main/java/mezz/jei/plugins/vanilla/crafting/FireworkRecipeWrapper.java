package mezz.jei.plugins.vanilla.crafting;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;

public class FireworkRecipeWrapper implements ICraftingRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final List<ItemStack> outputs;

	public FireworkRecipeWrapper(List<List<ItemStack>> inputs, List<ItemStack> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public FireworkRecipeWrapper(List<List<ItemStack>> inputs, ItemStack output) {
		this(inputs, Collections.singletonList(output));
	}

	List<List<ItemStack>> getInputs() {
		return inputs;
	}

	List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputs));
	}
}
