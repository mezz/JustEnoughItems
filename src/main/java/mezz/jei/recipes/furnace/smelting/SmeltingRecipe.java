package mezz.jei.recipes.furnace.smelting;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class SmeltingRecipe {
	@Nonnull
	private final List<ItemStack> input;
	@Nonnull
	private final ItemStack output;
	private final float experience;

	public SmeltingRecipe(@Nonnull List<ItemStack> input, @Nonnull ItemStack output, float experience) {
		this.input = input;
		this.output = output;
		this.experience = experience;
	}

	@Nonnull
	public List<ItemStack> getInput() {
		return input;
	}

	@Nonnull
	public ItemStack getOutput() {
		return output;
	}

	public float getExperience() {
		return experience;
	}
}
