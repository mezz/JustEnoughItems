package mezz.jei.plugins.vanilla.compostable;

import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class CompostableRecipe {
	private final List<ItemStack> inputs;
	private final float chance;

	public CompostableRecipe(ItemStack input, float chance) {
		this.inputs = Collections.singletonList(input);
		this.chance = chance;
	}

	public List<ItemStack> getInputs() {
		return inputs;
	}

	public float getChance() {
		return chance;
	}
}
