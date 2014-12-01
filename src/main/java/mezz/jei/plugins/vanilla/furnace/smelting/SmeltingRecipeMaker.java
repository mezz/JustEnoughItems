package mezz.jei.plugins.vanilla.furnace.smelting;

import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmeltingRecipeMaker {

	@Nonnull
	public static List<SmeltingRecipe> getFurnaceRecipes() {
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
		Map<ItemStack, ItemStack> smeltingMap = getSmeltingMap(furnaceRecipes);

		List<SmeltingRecipe> recipes = new ArrayList<SmeltingRecipe>();

		for (Map.Entry<ItemStack, ItemStack> itemStackItemStackEntry : smeltingMap.entrySet()) {
			ItemStack input = itemStackItemStackEntry.getKey();
			ItemStack output = itemStackItemStackEntry.getValue();

			float experience = furnaceRecipes.func_151398_b(output);

			List<ItemStack> inputs = StackUtil.getSubtypes(input);
			SmeltingRecipe recipe = new SmeltingRecipe(inputs, output, experience);
			recipes.add(recipe);
		}

		return recipes;
	}

	@SuppressWarnings("unchecked")
	private static Map<ItemStack, ItemStack> getSmeltingMap(@Nonnull FurnaceRecipes furnaceRecipes) {
		return furnaceRecipes.getSmeltingList();
	}
}
