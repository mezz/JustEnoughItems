package mezz.jei.api.recipe.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.text.ITextComponent;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayout, T, IIngredients)}.
 * Also draws elements that are common to all recipes in the category like the background.
 */
public interface IRecipeCategory<T> {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 *
	 * @see VanillaRecipeCategoryUid for vanilla examples
	 */
	ResourceLocation getUid();

	Class<? extends T> getRecipeClass();

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 */
	String getTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 *
	 * The size of the background determines how recipes are laid out by JEI,
	 * make sure it is the right size to contains everything being displayed.
	 */
	IDrawable getBackground();

	/**
	 * Icon for the category tab.
	 * You can use {@link IGuiHelper#createDrawableIngredient(Object)} to create a drawable from an ingredient.
	 *
	 * @return icon to draw on the category tab, max size is 16x16 pixels.
	 */
	IDrawable getIcon();

	/**
	 * Sets all the recipe's ingredients by filling out an instance of {@link IIngredients}.
	 * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
	 */
	void setIngredients(T recipe, IIngredients ingredients);

	/**
	 * Set the {@link IRecipeLayout} properties from the recipe.
	 *
	 * @param recipeLayout  the layout that needs its properties set.
	 * @param recipe        the recipe, for extra information.
	 * @param ingredients   the ingredients, already set earlier by {@link IRecipeCategory#setIngredients}
	 */
	void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients);

	/**
	 * Draw extras or additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltipStrings(Object, double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 */
	default void draw(T recipe, MatrixStack matrixStack, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever's under the mouse.
	 * Ingredient tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 */
	default List<ITextComponent> getTooltipStrings(T recipe, double mouseX, double mouseY) {
		return Collections.emptyList();
	}

	/**
	 * Called when a player clicks the recipe.
	 * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
	 *
	 * @param mouseX      the X position of the mouse, relative to the recipe.
	 * @param mouseY      the Y position of the mouse, relative to the recipe.
	 * @param mouseButton the current mouse event button.
	 * @return true if the click was handled, false otherwise
	 */
	default boolean handleClick(T recipe, double mouseX, double mouseY, int mouseButton) {
		return false;
	}
}
