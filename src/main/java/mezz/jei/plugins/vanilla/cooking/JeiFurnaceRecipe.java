package mezz.jei.plugins.vanilla.cooking;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class JeiFurnaceRecipe extends FurnaceRecipe {
	private final Ingredient fuel;
	private final ItemStack fuelOutput;

	public JeiFurnaceRecipe(
		ResourceLocation id,
		Ingredient input,
		Ingredient fuel,
		ItemStack fuelOutput,
		ItemStack output,
		float experience,
		int cookingTime
	) {
		super(id, "", input, output, experience, cookingTime);
		this.fuel = fuel;
		this.fuelOutput = fuelOutput;
	}

	public Ingredient getFuel() {
		return fuel;
	}

	public ItemStack getFuelOutput() {
		return fuelOutput;
	}
}
