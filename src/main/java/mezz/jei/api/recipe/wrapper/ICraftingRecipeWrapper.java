package mezz.jei.api.recipe.wrapper;

import java.util.List;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

/**
 * Implement this interface instead of just {@link IRecipeWrapper} to have your recipe wrapper work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shapeless recipe.
 * <p>
 * For shaped recipes, use {@link IShapedCraftingRecipeWrapper}.
 * To override the category's behavior and set the recipe layout yourself, use {@link ICustomCraftingRecipeWrapper}.
 */
public interface ICraftingRecipeWrapper extends IRecipeWrapper {

	@Override
	@Deprecated
	List getInputs();

	@Override
	@Deprecated
	List<ItemStack> getOutputs();

}
