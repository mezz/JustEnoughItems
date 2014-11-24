package mezz.jei.api.recipe;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IRecipeWrapper {
	/* Returns the input List for the recipe. */
	List getInputs();

	/* Returns the output List for the recipe. */
	List<ItemStack> getOutputs();
}
