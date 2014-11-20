package mezz.jei.recipes.furnace.fuel;

import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FuelRecipe {
	private final List<ItemStack> input;

	public FuelRecipe(ItemStack input) {
		this.input = Arrays.asList(input);
	}

	public FuelRecipe(List<ItemStack> input) {
		this.input = input;
	}

	public List<ItemStack> getInput() {
		return input;
	}
}
