package mezz.jei.recipe.furnace.fuel;

import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FuelRecipeMaker {

	@Nonnull
	public static List<FuelRecipe> getFuelRecipes(@Nonnull List<ItemStack> fuelStacks) {
		Set<String> oreDictNames = new HashSet<String>();
		List<FuelRecipe> fuelRecipes = new ArrayList<FuelRecipe>(fuelStacks.size());
		for (ItemStack fuelStack : fuelStacks) {
			if (fuelStack == null)
				continue;

			int[] oreIDs = OreDictionary.getOreIDs(fuelStack);
			if (oreIDs.length > 0) {
				for (int oreID: oreIDs) {
					String name = OreDictionary.getOreName(oreID);
					if (oreDictNames.contains(name))
						continue;

					oreDictNames.add(name);
					List<ItemStack> oreDictFuels = OreDictionary.getOres(name);
					Set<ItemStack> oreDictFuelsSet = StackUtil.getAllSubtypesSet(oreDictFuels);
					fuelRecipes.add(new FuelRecipe(oreDictFuelsSet));
				}
			} else {
				List<ItemStack> fuels = StackUtil.getSubtypes(fuelStack);
				fuelRecipes.add(new FuelRecipe(fuels));
			}
		}
		return fuelRecipes;
	}
}
