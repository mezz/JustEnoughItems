package mezz.jei.recipes.furnace;

import net.minecraft.item.ItemStack;

import java.util.List;

public class FurnaceRecipe {
	private final List<ItemStack> input;
	private final ItemStack output;
	private final float experience;

	public FurnaceRecipe(List<ItemStack> input, ItemStack output, float experience) {
		this.input = input;
		this.output = output;
		this.experience = experience;
	}

	public List<ItemStack> getInput() {
		return input;
	}

	public ItemStack getOutput() {
		return output;
	}

	public float getExperience() {
		return experience;
	}
}
