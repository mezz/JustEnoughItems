package mezz.jei.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IStackHelper;

public final class FuelRecipeMaker {

	private FuelRecipeMaker() {
	}

	public static List<FuelRecipe> getFuelRecipes(IIngredientRegistry ingredientRegistry, IJeiHelpers helpers) {
		IGuiHelper guiHelper = helpers.getGuiHelper();
		IStackHelper stackHelper = helpers.getStackHelper();
		List<ItemStack> fuelStacks = ingredientRegistry.getFuels();
		Int2BooleanMap oreIdsHaveRecipe = new Int2BooleanArrayMap();
		List<FuelRecipe> fuelRecipes = new ArrayList<>(fuelStacks.size());
		for (ItemStack fuelStack : fuelStacks) {
			int burnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
			List<ItemStack> subtypes = stackHelper.getSubtypes(fuelStack);
			List<ItemStack> fuels = new ArrayList<>();
			for (ItemStack subtype : subtypes) {
				if (TileEntityFurnace.getItemBurnTime(subtype) == burnTime) {
					fuels.add(subtype);
				}
			}
			if (fuels.isEmpty()) {
				fuels.add(fuelStack);
			}
			if (fuels.size() <= 1) {
				int[] oreIDs = OreDictionary.getOreIDs(fuelStack);
				boolean hasOreRecipe = false;
				for (int oreId : oreIDs) {
					if (!oreIdsHaveRecipe.containsKey(oreId)) {
						String oreName = OreDictionary.getOreName(oreId);
						List<ItemStack> ores = stackHelper.getAllSubtypes(OreDictionary.getOres(oreName));
						if (ores.size() > 1 && ores.stream().allMatch(itemStack -> TileEntityFurnace.getItemBurnTime(itemStack) == burnTime)) {
							oreIdsHaveRecipe.put(oreId, true);
							fuelRecipes.add(new FuelRecipe(guiHelper, ores, burnTime));
						} else {
							oreIdsHaveRecipe.put(oreId, false);
						}
					}
					hasOreRecipe |= oreIdsHaveRecipe.get(oreId);
				}
				if (!hasOreRecipe) {
					fuelRecipes.add(new FuelRecipe(guiHelper, fuels, burnTime));
				}
			} else {
				fuelRecipes.add(new FuelRecipe(guiHelper, fuels, burnTime));
			}
		}
		return fuelRecipes;
	}

}
