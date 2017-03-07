package mezz.jei.plugins.vanilla.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public final class SmeltingRecipeMaker {

	private SmeltingRecipeMaker() {
	}

	public static List<SmeltingRecipe> getFurnaceRecipes(IJeiHelpers helpers) {
		IStackHelper stackHelper = helpers.getStackHelper();
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
		Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

		List<SmeltingRecipe> recipes = new ArrayList<>();

		for (Map.Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
			ItemStack input = entry.getKey();
			ItemStack output = entry.getValue();
			if (input == null || output == null) {
				Log.get().error("Found invalid smelting recipe: ({} -> {})", ErrorUtil.getItemStackInfo(input), ErrorUtil.getItemStackInfo(output));
			} else {
				List<ItemStack> inputs = stackHelper.getSubtypes(input);
				SmeltingRecipe recipe = new SmeltingRecipe(inputs, output);
				recipes.add(recipe);
			}
		}

		return recipes;
	}

}
