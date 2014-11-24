package mezz.jei.recipe.furnace.smelting;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class SmeltingRecipe implements IRecipeWrapper {
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
	public List<ItemStack> getInputs() {
		return input;
	}

	@Nonnull
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(output);
	}

	public float getExperience() {
		return experience;
	}

}
