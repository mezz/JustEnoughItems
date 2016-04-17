package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ShapelessRecipesWrapper extends AbstractShapelessRecipeWrapper {

	@Nonnull
	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(@Nonnull IGuiHelper guiHelper, @Nonnull ShapelessRecipes recipe) {
		super(guiHelper);
		this.recipe = recipe;
		for (Object input : this.recipe.recipeItems) {
			if (input instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) input;
				if (itemStack.stackSize != 1) {
					itemStack.stackSize = 1;
				}
			}
		}
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs() {
		return recipe.recipeItems;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getRecipeOutput());
	}
}
