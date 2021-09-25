package mezz.jei.plugins.vanilla.compostable;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class CompostableRecipeMaker {
	public static List<CompostableRecipe> getRecipes(IIngredientManager ingredientManager) {
		List<CompostableRecipe> recipes = new ArrayList<>();
		Object2FloatMap<IItemProvider> compostables = ComposterBlock.COMPOSTABLES;
		Collection<ItemStack> allItemStacks = ingredientManager.getAllIngredients(VanillaTypes.ITEM);
		for (ItemStack itemStack : allItemStacks) {
			Item item = itemStack.getItem();
			float compostValue = compostables.getOrDefault(item, 0);
			if (compostValue > 0) {
				CompostableRecipe recipe = new CompostableRecipe(itemStack, compostValue);
				recipes.add(recipe);
			}
		}
		recipes.sort(Comparator.comparingDouble(CompostableRecipe::getChance));
		return recipes;
	}
}
