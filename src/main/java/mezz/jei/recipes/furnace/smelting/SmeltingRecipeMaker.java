package mezz.jei.recipes.furnace.smelting;

import mezz.jei.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SmeltingRecipeMaker {

	public static List<SmeltingRecipe> getFurnaceRecipes(FurnaceRecipes furnaceRecipes) {
		Map<ItemStack, ItemStack> smeltingMap = getSmeltingMap(furnaceRecipes);

		List<SmeltingRecipe> recipes = new ArrayList<SmeltingRecipe>();

		for (ItemStack input : smeltingMap.keySet()) {
			ItemStack output = smeltingMap.get(input);

			float experience = furnaceRecipes.func_151398_b(output);

			List<ItemStack> inputs;
			if (input.getItemDamage() == OreDictionary.WILDCARD_VALUE && input.getHasSubtypes()) {
				Item item = input.getItem();
				inputs = StackUtil.getSubItems(item);
			} else {
				inputs = Arrays.asList(input);
			}
			SmeltingRecipe recipe = new SmeltingRecipe(inputs, output, experience);
			recipes.add(recipe);
		}

		return recipes;
	}

	@SuppressWarnings("unchecked")
	private static Map<ItemStack, ItemStack> getSmeltingMap(FurnaceRecipes furnaceRecipes) {
		return furnaceRecipes.getSmeltingList();
	}
}
