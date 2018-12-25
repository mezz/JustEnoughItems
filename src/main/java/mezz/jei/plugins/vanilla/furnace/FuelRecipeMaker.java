package mezz.jei.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;

public final class FuelRecipeMaker {

	private FuelRecipeMaker() {
	}

	public static List<FuelRecipe> getFuelRecipes(IIngredientRegistry ingredientRegistry, IJeiHelpers helpers) {
		IGuiHelper guiHelper = helpers.getGuiHelper();
		List<ItemStack> fuelStacks = ingredientRegistry.getFuels();
		List<FuelRecipe> fuelRecipes = new ArrayList<>(fuelStacks.size());
		Map<Item, Integer> burnTimes = TileEntityFurnace.getBurnTimes();
		for (ItemStack fuelStack : fuelStacks) {
			int burnTime = getItemBurnTime(fuelStack, burnTimes);
			fuelRecipes.add(new FuelRecipe(guiHelper, Collections.singleton(fuelStack), burnTime));
		}
		return fuelRecipes;
	}

	private static int getItemBurnTime(ItemStack stack, Map<Item, Integer> burnTimes) {
		if (stack.isEmpty()) {
			return 0;
		}
		int ret = stack.getBurnTime();
		if (ret == -1) {
			Item item = stack.getItem();
			ret = burnTimes.getOrDefault(item, 0);
		}
		return ForgeEventFactory.getItemBurnTime(stack, ret);
	}

}
