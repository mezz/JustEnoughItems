package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import net.minecraft.world.item.crafting.CraftingRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

/**
 * This allows you to register extensions to vanilla recipe categories, to customize their behavior.
 *
 * An instance of this is passed to you mod's plugin in
 * {@link IModPlugin#registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration)}
 */
public interface IVanillaCategoryExtensionRegistration {
	/**
	 * Get the vanilla crafting category, to extend it with your own mod's crafting category extensions.
	 */
	IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> getCraftingCategory();
}
