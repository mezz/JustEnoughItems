package mezz.jei.recipes.furnace.fuel;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class FuelRecipe {
	@Nonnull
	private final List<ItemStack> input;

	public FuelRecipe(@Nonnull List<ItemStack> input) {
		this.input = input;
	}

	@Nonnull
	public List<ItemStack> getInput() {
		return input;
	}
}
