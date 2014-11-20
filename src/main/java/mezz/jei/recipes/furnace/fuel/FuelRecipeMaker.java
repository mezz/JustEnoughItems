package mezz.jei.recipes.furnace.fuel;

import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FuelRecipeMaker {

	public static List<FuelRecipe> getFuelRecipes(List<ItemStack> fuelStacks) {
		Set<String> oreDictNames = new HashSet<String>();
		List<FuelRecipe> fuelRecipes = new ArrayList<FuelRecipe>(fuelStacks.size());
		for (ItemStack fuelStack : fuelStacks) {
			int[] oreIDs = OreDictionary.getOreIDs(fuelStack);
			if (oreIDs.length > 0) {
				for (int oreID: oreIDs) {
					String name = OreDictionary.getOreName(oreID);
					if (oreDictNames.contains(name))
						continue;

					oreDictNames.add(name);
					List<ItemStack> oreDictFuels = OreDictionary.getOres(name);
					oreDictFuels = StackUtil.getItemStacksRecursive(oreDictFuels);
					fuelRecipes.add(new FuelRecipe(oreDictFuels));
				}
			} else {
				List<ItemStack> fuels = Arrays.asList(fuelStack);
				fuels = StackUtil.getItemStacksRecursive(fuels);
				fuelRecipes.add(new FuelRecipe(fuels));
			}
		}
		return fuelRecipes;
	}
}
