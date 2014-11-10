package mezz.jei.api.recipes;

import mezz.jei.api.gui.IRecipeGui;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * An IRecipeHelper provides information about one type of recipe.
 */
public interface IRecipeHelper {

	/* Returns the page title for this type of recipe. */
	String getTitle();

	/* Returns the class of the Recipe handled by this IRecipeHelper. */
	Class getRecipeClass();

	/* Returns a new IRecipeGui instance. */
	IRecipeGui createGui();

	/* Returns all input ItemStacks for the recipe. */
	List<ItemStack> getInputs(Object recipe);

	/* Returns all output ItemStacks for the recipe. */
	List<ItemStack> getOutputs(Object recipe);

}
