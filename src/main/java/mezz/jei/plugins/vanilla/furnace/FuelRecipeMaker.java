package mezz.jei.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.OreDictionary;

public class FuelRecipeMaker {

	public static List<FuelRecipe> getFuelRecipes(IIngredientRegistry ingredientRegistry, IJeiHelpers helpers) {
		IGuiHelper guiHelper = helpers.getGuiHelper();
		IStackHelper stackHelper = helpers.getStackHelper();
		List<ItemStack> fuelStacks = ingredientRegistry.getFuels();
		Set<String> oreDictNames = new HashSet<String>();
		List<FuelRecipe> fuelRecipes = new ArrayList<FuelRecipe>(fuelStacks.size());
		for (ItemStack fuelStack : fuelStacks) {
			if (fuelStack == null) {
				continue;
			}

			int[] oreIDs = OreDictionary.getOreIDs(fuelStack);
			if (oreIDs.length > 0) {
				for (int oreID : oreIDs) {
					String name = OreDictionary.getOreName(oreID);
					if (oreDictNames.contains(name)) {
						continue;
					}

					oreDictNames.add(name);
					List<ItemStack> oreDictFuels = OreDictionary.getOres(name);
					Collection<ItemStack> oreDictFuelsSet = stackHelper.getAllSubtypes(oreDictFuels);
					removeNoBurnTime(oreDictFuelsSet);
					if (oreDictFuels.isEmpty()) {
						continue;
					}
					int burnTime = getBurnTime(oreDictFuels.get(0));

					fuelRecipes.add(new FuelRecipe(guiHelper, oreDictFuelsSet, burnTime));
				}
			} else {
				List<ItemStack> fuels = stackHelper.getSubtypes(fuelStack);
				removeNoBurnTime(fuels);
				if (fuels.isEmpty()) {
					continue;
				}
				int burnTime = getBurnTime(fuels.get(0));
				fuelRecipes.add(new FuelRecipe(guiHelper, fuels, burnTime));
			}
		}
		return fuelRecipes;
	}

	private static void removeNoBurnTime(Collection<ItemStack> itemStacks) {
		Iterator<ItemStack> iterator = itemStacks.iterator();
		while (iterator.hasNext()) {
			ItemStack itemStack = iterator.next();
			if (getBurnTime(itemStack) == 0) {
				iterator.remove();
			}
		}
	}

	private static int getBurnTime(ItemStack itemStack) {
		return TileEntityFurnace.getItemBurnTime(itemStack);
	}
}
