package mezz.jei.api.recipe.wrapper;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ICraftingRecipeWrapper extends IRecipeWrapper {

	@Override
	List<ItemStack> getOutputs();

}
