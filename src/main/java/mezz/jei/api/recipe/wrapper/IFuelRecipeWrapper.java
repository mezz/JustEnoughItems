package mezz.jei.api.recipe.wrapper;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IFuelRecipeWrapper extends IRecipeWrapper {

	@Nonnull
	@Override
	List<ItemStack> getInputs();

}
