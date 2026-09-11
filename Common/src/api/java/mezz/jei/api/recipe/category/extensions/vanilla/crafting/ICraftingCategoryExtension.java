package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#CRAFTING} recipes.
 *
 * For shaped recipes, override {@link #getWidth(RecipeHolder)} and {@link #getHeight(RecipeHolder)}.
 *
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 * and then registering it with {@link IExtendableCraftingRecipeCategory#addExtension(Class, ICraftingCategoryExtension)}.
 *
 * @apiNote Since 16.0.0, extensions have the recipe passed to them in each method,
 * so they can be singleton instances instead of creating many of them to wrap recipes.
 */
public interface ICraftingCategoryExtension<R extends CraftingRecipe> extends IRecipeCategoryExtension<RecipeHolder<R>> {
	/**
	 * Get the inputs for the given crafting recipe.
	 * @since 20.0.0
	 */
	List<SlotDisplay> getIngredients(RecipeHolder<R> recipeHolder);

	/**
	 * @return the width of a shaped recipe, or 0 for a shapeless recipe
	 * @since 16.0.0
	 */
	default int getWidth(RecipeHolder<R> recipeHolder) {
		return 0;
	}

	/**
	 * @return the height of a shaped recipe, or 0 for a shapeless recipe
	 * @since 16.0.0
	 */
	default int getHeight(RecipeHolder<R> recipeHolder) {
		return 0;
	}

	/**
	 * Override the default {@link IRecipeCategory} behavior.
	 *
	 * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
	 *
	 * @since 21.3.0
	 */
	default void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		R recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		SlotDisplay resultItem = display.result();
		craftingGridHelper.createAndSetOutputs(builder, resultItem);

		List<SlotDisplay> ingredients = getIngredients(recipeHolder);
		int width = getWidth(recipeHolder);
		int height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, ingredients, width, height);
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
	 * @param recipeHolder the current crafting recipe being drawn.
	 * @param recipeSlots the current recipe slots being drawn.
	 * @param focuses the current focuses
	 *
	 * @see IRecipeCategory#onDisplayedIngredientsUpdate
	 *
	 * @since 19.14.2
	 */
	default void onDisplayedIngredientsUpdate(
		RecipeHolder<R> recipeHolder,
		List<IRecipeSlotDrawable> recipeSlots,
		IFocusGroup focuses
	) {

	}
}
