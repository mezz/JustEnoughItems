package mezz.jei.plugins.vanilla.crafting;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessOreRecipeWrapper extends AbstractShapelessRecipeWrapper {

	private final ShapelessOreRecipe recipe;

	public ShapelessOreRecipeWrapper(IGuiHelper guiHelper, ShapelessOreRecipe recipe) {
		super(guiHelper);
		this.recipe = recipe;
		for (Object input : this.recipe.getInput()) {
			if (input instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) input;
				if (itemStack.stackSize != 1) {
					itemStack.stackSize = 1;
				}
			}
		}
	}

	@Override
	public List getInputs() {
		return recipe.getInput();
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}
}
