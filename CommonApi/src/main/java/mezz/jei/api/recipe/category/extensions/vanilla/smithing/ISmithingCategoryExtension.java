package mezz.jei.api.recipe.category.extensions.vanilla.smithing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.SmithingRecipe;


/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#SMITHING} recipe.
 *
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getSmithingCategory()}
 * and then registering it with {@link IExtendableSmithingRecipeCategory#addExtension(Class, ISmithingCategoryExtension)}.
 *
 * @since 19.5.0
 */
public interface ISmithingCategoryExtension<R extends SmithingRecipe> {
	/**
	 * Set the template ingredient for the recipe.
	 *
	 * For example, see
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe#template}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe#template}
	 *
	 * @since 19.5.0
	 */
	<T extends IIngredientAcceptor<T>> void setTemplate(R recipe, T ingredientAcceptor);

	/**
	 * Set the base ingredient for the recipe.
	 *
	 * For example, see
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe#base}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe#base}
	 *
	 * @since 19.5.0
	 */
	<T extends IIngredientAcceptor<T>> void setBase(R recipe, T ingredientAcceptor);

	/**
	 * Set the addition ingredient for the recipe.
	 *
	 * For example, see
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe#addition}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe#addition}
	 *
	 * @since 19.5.0
	 */
	<T extends IIngredientAcceptor<T>> void setAddition(R recipe, T ingredientAcceptor);

	/**
	 * Set some example output ingredients for the recipe.
	 * Don't set all the outputs if there are hundreds, it'll just waste memory.
	 *
	 * For example, see the results of
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe#assemble}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe#assemble}
	 *
	 * @since 19.8.3
	 */
	default <T extends IIngredientAcceptor<T>> void setOutput(R recipe, T ingredientAcceptor) {

	}

	/**
	 * Called every time JEI updates the cycling displayed ingredients on a recipe.
	 *
	 * Use this (for example) to compute recipe outputs that result from complex relationships between ingredients.
	 *
	 * Use {@link IRecipeSlotDrawable#getDisplayedIngredient()} from your regular slots to see what is
	 * currently being drawn, and calculate what you need from there.
	 * You can override any slot's displayed ingredient with {@link IRecipeSlotDrawable#createDisplayOverrides()}.
	 *
	 * Note that overrides set this way are not searchable via recipe lookups in JEI,
	 * it is only for displaying things too complex for normal lookups to handle.
	 *
	 * @param recipe the current smithing recipe being drawn.
	 * @param templateSlot the current smithing recipe template slot being drawn.
	 * @param baseSlot the current smithing recipe base slot being drawn.
	 * @param additionSlot the current smithing recipe addition slot being drawn.
	 * @param outputSlot the current smithing recipe output slot being drawn.
	 * @param focuses the current focuses
	 *
	 * @see IRecipeCategory#onDisplayedIngredientsUpdate
	 *
	 * @since 19.14.1
	 */
	default void onDisplayedIngredientsUpdate(
		R recipe,
		IRecipeSlotDrawable templateSlot,
		IRecipeSlotDrawable baseSlot,
		IRecipeSlotDrawable additionSlot,
		IRecipeSlotDrawable outputSlot,
		IFocusGroup focuses
	) {

	}
}
