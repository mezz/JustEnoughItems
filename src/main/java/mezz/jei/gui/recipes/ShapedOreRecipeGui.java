package mezz.jei.gui.recipes;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedOreRecipeGui extends CraftingRecipeGui {

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack itemStack) {
		ShapedOreRecipe shapedOreRecipe = (ShapedOreRecipe)recipe;

		int width;
		int height;
		try {
			width = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, shapedOreRecipe, "width");
			height = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, shapedOreRecipe, "height");
		} catch (Exception e) {
			Log.error("Error loading recipe", e);
			return;
		}

		setInput(shapedOreRecipe.getInput(), itemStack, width, height);
		setOutput(shapedOreRecipe.getRecipeOutput());
	}

}
