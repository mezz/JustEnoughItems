package mezz.jei.api.recipe.category.extensions.vanilla.smithing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.UpgradeRecipe;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#SMITHING} recipe.
 *
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getSmithingCategory()}
 * and then registering it with {@link IExtendableSmithingRecipeCategory#addExtension(Class, ISmithingCategoryExtension)}.
 *
 * @since 11.34.0
 */
public interface ISmithingCategoryExtension<R extends UpgradeRecipe> {
	/**
	 * Set the base ingredient for the recipe.
	 *
	 * For example, see {@link UpgradeRecipe#base}
	 *
	 * @since 11.34.0
	 */
	<T extends IIngredientAcceptor<T>> void setBase(R recipe, T ingredientAcceptor);

	/**
	 * Set the addition ingredient for the recipe.
	 *
	 * For example, see {@link UpgradeRecipe#addition}
	 *
	 * @since 11.34.0
	 */
	<T extends IIngredientAcceptor<T>> void setAddition(R recipe, T ingredientAcceptor);

	/**
	 * Set some example output ingredients for the recipe.
	 * Don't set all the outputs if there are hundreds, it'll just waste memory.
	 *
	 * For example, see the results of {@link UpgradeRecipe#getResultItem()}.
	 *
	 * @since 11.34.1
	 */
	default <T extends IIngredientAcceptor<T>> void setOutput(R recipe, T ingredientAcceptor) {

	}
}
