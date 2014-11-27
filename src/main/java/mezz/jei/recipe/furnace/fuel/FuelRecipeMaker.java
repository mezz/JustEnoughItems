package mezz.jei.recipe.furnace.fuel;

import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
					Collection<ItemStack> oreDictFuelsSet = StackUtil.getAllSubtypes(oreDictFuels);
					removeNoBurnTime(oreDictFuelsSet);
					if (oreDictFuels.isEmpty())
						continue;
					int burnTime = getBurnTime(oreDictFuels.get(0));

					fuelRecipes.add(new FuelRecipe(oreDictFuelsSet, burnTime));
				}
			} else {
				List<ItemStack> fuels = StackUtil.getSubtypes(fuelStack);
				removeNoBurnTime(fuels);
				if (fuels.isEmpty())
					continue;
				int burnTime = getBurnTime(fuels.get(0));
				fuelRecipes.add(new FuelRecipe(fuels, burnTime));
			}
		}
		return fuelRecipes;
	}

	private static void removeNoBurnTime(Collection<ItemStack> itemStacks) {
		Iterator<ItemStack> iter = itemStacks.iterator();
		while (iter.hasNext()) {
			ItemStack itemStack = iter.next();
			if (getBurnTime(itemStack) == 0)
				iter.remove();
		}
	}

	private static int getBurnTime(ItemStack itemStack) {
		return TileEntityFurnace.getItemBurnTime(itemStack);
	}
}
