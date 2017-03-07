package mezz.jei.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class SmeltingRecipeMaker {

	public static List<SmeltingRecipe> getFurnaceRecipes(IJeiHelpers helpers) {
		IStackHelper stackHelper = helpers.getStackHelper();
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
		Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

		List<SmeltingRecipe> recipes = new ArrayList<SmeltingRecipe>();

		for (Map.Entry<ItemStack, ItemStack> itemStackItemStackEntry : smeltingMap.entrySet()) {
			ItemStack input = itemStackItemStackEntry.getKey();
			ItemStack output = itemStackItemStackEntry.getValue();
			if (input == null || output == null) {
				Log.error("Found invalid smelting recipe: ({} -> {})", ErrorUtil.getItemStackInfo(input), ErrorUtil.getItemStackInfo(output));
			} else {
				List<ItemStack> inputs = stackHelper.getSubtypes(input);
				SmeltingRecipe recipe = new SmeltingRecipe(inputs, output);
				recipes.add(recipe);
			}
		}

		return recipes;
	}

}
