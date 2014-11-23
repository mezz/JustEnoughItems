package mezz.jei.recipes.crafting;

import mezz.jei.gui.GuiItemStacks;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(@Nonnull GuiItemStacks guiItemStacks, @Nonnull Object recipe, @Nullable ItemStack focusStack) {
		ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe)recipe;

		ArrayList<Object> input = shapelessOreRecipe.getInput();
		for (int i = 0; i < input.size(); i++) {
			Object obj = input.get(i);
			if (obj instanceof ItemStack) {
				setInput(guiItemStacks, i, (ItemStack) obj);
			} else if (obj instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.getItemStacksRecursive((Iterable)obj);
				setInput(guiItemStacks, i, itemStacks, focusStack);
			}
		}

		setOutput(guiItemStacks, shapelessOreRecipe.getRecipeOutput());
	}

}
