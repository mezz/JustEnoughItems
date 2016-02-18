package mezz.jei.api.recipe.wrapper;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICraftingRecipeWrapper extends IRecipeWrapper {

	@Nonnull
	@Override
	List getInputs();

	@Nonnull
	@Override
	List<ItemStack> getOutputs();

}
