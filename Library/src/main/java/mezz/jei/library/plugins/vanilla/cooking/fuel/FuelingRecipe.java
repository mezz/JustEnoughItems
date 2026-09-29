package mezz.jei.library.plugins.vanilla.cooking.fuel;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class FuelingRecipe implements IJeiFuelingRecipe {
	private final List<ItemStack> inputs;
	private final int burnTime;
	private final float smeltCount;

	public FuelingRecipe(Collection<ItemStack> input, int burnTime, float speedMultiplier) {
		Preconditions.checkArgument(burnTime > 0, "burn time must be greater than 0");
		this.inputs = List.copyOf(input);
		this.burnTime = burnTime;
		// Match the furnace's whole-tick cooking time for a standard 200-tick recipe.
		int cookingTime = 200;
		if (speedMultiplier > 0) {
			cookingTime = Math.max(1, (int) Math.ceil(200 / speedMultiplier));
		}
		this.smeltCount = (float) burnTime / cookingTime;
	}

	@Override
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Override
	public int getBurnTime() {
		return burnTime;
	}

	@Override
	public float getSmeltCount() {
		return smeltCount;
	}
}
