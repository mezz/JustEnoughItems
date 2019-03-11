package mezz.jei.api.registration;

import net.minecraft.item.crafting.IRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

public interface IVanillaCategoryExtensionRegistration {
	/**
	 * Get the vanilla crafting category, to extend it with your own mod's crafting category extensions.
	 */
	IExtendableRecipeCategory<IRecipe, ICraftingCategoryExtension> getCraftingCategory();
}
