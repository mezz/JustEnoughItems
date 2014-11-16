package mezz.jei.api.recipes;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * An IRecipeHelper provides information about one Recipe Class.
 */
public interface IRecipeHelper {

	/* Returns the class of the Recipe handled by this IRecipeHelper. */
	Class getRecipeClass();

	/* Returns the type of this recipe. */
	IRecipeType getRecipeType();

	/* Returns a new IRecipeGui instance. */
	IRecipeGui createGui();

	/* Returns all input ItemStacks for the recipe. */
	List<ItemStack> getInputs(Object recipe);

	/* Returns all output ItemStacks for the recipe. */
	List<ItemStack> getOutputs(Object recipe);

}
