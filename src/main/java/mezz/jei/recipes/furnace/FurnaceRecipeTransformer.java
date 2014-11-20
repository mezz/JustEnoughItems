package mezz.jei.recipes.furnace;

import mezz.jei.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FurnaceRecipeTransformer {

	public static List<FurnaceRecipe> getFurnaceRecipes(FurnaceRecipes furnaceRecipes) {
		Map<ItemStack, ItemStack> smeltingMap = getSmeltingMap(furnaceRecipes);

		List<FurnaceRecipe> recipes = new ArrayList<FurnaceRecipe>();

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
			FurnaceRecipe recipe = new FurnaceRecipe(inputs, output, experience);
			recipes.add(recipe);
		}

		return recipes;
	}

	@SuppressWarnings("unchecked")
	private static Map<ItemStack, ItemStack> getSmeltingMap(FurnaceRecipes furnaceRecipes) {
		return furnaceRecipes.getSmeltingList();
	}
}
