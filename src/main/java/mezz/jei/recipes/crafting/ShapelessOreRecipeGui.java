package mezz.jei.recipes.crafting;

import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(@Nonnull Object recipe, @Nullable ItemStack focusStack) {
		ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe)recipe;

		ArrayList<Object> recipeItems = shapelessOreRecipe.getInput();
		for (int i = 0; i < recipeItems.size(); i++) {
			Object obj = recipeItems.get(i);
			if (obj instanceof ItemStack) {
				setInput(i, (ItemStack) obj);
			} else if (obj instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.getItemStacksRecursive((Iterable)obj);
				setInput(i, itemStacks, focusStack);
			}
		}

		setOutput(shapelessOreRecipe.getRecipeOutput());
	}

}
