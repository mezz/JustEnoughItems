package mezz.jei.api.recipe.category.extensions;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.util.text.ITextComponent;

/**
 * An extension to a recipe category with methods that allow JEI to make sense of it.
 * Plugins implement these for recipe categories that support it, for each type of recipe they have.
 */
public interface IRecipeCategoryExtension {

	/**
	 * Gets all the recipe's ingredients by filling out an instance of {@link IIngredients}.
	 */
	void setIngredients(IIngredients ingredients);

	/**
	 * Draw additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltipStrings(double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 */
	default void drawInfo(int recipeWidth, int recipeHeight, MatrixStack matrixStack, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever's under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
	 * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltipStrings(Object, double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 */
	default List<ITextComponent> getTooltipStrings(double mouseX, double mouseY) {
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
	default boolean handleClick(double mouseX, double mouseY, int mouseButton) {
		return false;
	}
}
