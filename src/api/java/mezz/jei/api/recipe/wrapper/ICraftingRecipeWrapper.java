package mezz.jei.api.recipe.wrapper;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 * Implement this interface instead of just {@link IRecipeWrapper} to have your recipe wrapper work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shapeless recipe.
 * <p>
 * For shaped recipes, use {@link IShapedCraftingRecipeWrapper}.
 * To override the category's behavior and set the recipe layout yourself, use {@link ICustomCraftingRecipeWrapper}.
 */
public interface ICraftingRecipeWrapper extends IRecipeWrapper {
	/**
	 * Return the registry name of the recipe here.
	 * With advanced tooltips on, this will show on the output item's tooltip.
	 *
	 * This will also show the modId when the recipe modId and output item modId do not match.
	 * This lets the player know where the recipe came from.
	 *
	 * @return the registry name of the recipe, or null if there is none
	 * @since JEI 4.7.10
	 */
	@Nullable
	default ResourceLocation getRegistryName() {
		return null;
	}
}
