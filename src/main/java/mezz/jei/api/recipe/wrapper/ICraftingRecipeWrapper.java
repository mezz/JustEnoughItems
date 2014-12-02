package mezz.jei.api.recipe.wrapper;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICraftingRecipeWrapper extends IRecipeWrapper {

	@Override
	List getInputs();

	@Override
	List<ItemStack> getOutputs();

	@Override
	void drawInfo(@Nonnull Minecraft minecraft);

}
