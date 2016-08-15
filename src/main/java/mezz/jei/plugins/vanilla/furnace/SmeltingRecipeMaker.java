package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class SmeltingRecipeMaker {

	@Nonnull
	public static List<SmeltingRecipe> getFurnaceRecipes(IJeiHelpers helpers) {
		IStackHelper stackHelper = helpers.getStackHelper();
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
		Map<ItemStack, ItemStack> smeltingMap = furnaceRecipes.getSmeltingList();

		List<SmeltingRecipe> recipes = new ArrayList<SmeltingRecipe>();

		for (Map.Entry<ItemStack, ItemStack> itemStackItemStackEntry : smeltingMap.entrySet()) {
			ItemStack input = itemStackItemStackEntry.getKey();
			ItemStack output = itemStackItemStackEntry.getValue();

			List<ItemStack> inputs = stackHelper.getSubtypes(input);
			SmeltingRecipe recipe = new SmeltingRecipe(inputs, output);
			recipes.add(recipe);
		}

		return recipes;
	}

}
