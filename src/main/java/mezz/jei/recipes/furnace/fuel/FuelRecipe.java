package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.recipes.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FuelRecipe implements IRecipeWrapper {
	@Nonnull
	private final List<ItemStack> input;

	public FuelRecipe(@Nonnull Collection<ItemStack> input) {
		this.input = new ArrayList<ItemStack>(input);
	}

	@Override
	public List<ItemStack> getInputs() {
		return input;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.emptyList();
	}

}
