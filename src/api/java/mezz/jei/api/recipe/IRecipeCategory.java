package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}.
 * Also draws elements that are common to all recipes in the category like the background.
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 *
	 * @see VanillaRecipeCategoryUid for vanilla examples
	 */
	String getUid();

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 */
	String getTitle();

	/**
	 * Return the mod name or id associated with this recipe category.
	 * Used for the recipe category tab's tooltip.
	 *
	 * @since JEI 4.5.0
	 */
	String getModName();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 *
	 * The size of the background determines how recipes are laid out by JEI,
	 * make sure it is the right size to contains everything being displayed.
	 */
	IDrawable getBackground();

	/**
	 * Optional icon for the category tab.
	 * If no icon is defined here, JEI will use first item registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}
	 *
	 * @return icon to draw on the category tab, max size is 16x16 pixels.
	 * @since 3.13.1
	 */
	@Nullable
	default IDrawable getIcon() {
		return null;
	}

	/**
	 * Draw any extra elements that might be necessary, icons or extra slots.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 */
	default void drawExtras(Minecraft minecraft) {

	}

	/**
	 * Set the {@link IRecipeLayout} properties from the {@link IRecipeWrapper} and {@link IIngredients}.
	 *
	 * @param recipeLayout  the layout that needs its properties set.
	 * @param recipeWrapper the recipeWrapper, for extra information.
	 * @param ingredients   the ingredients, already set by the recipeWrapper
	 * @since JEI 3.11.0
	 */
	void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper, IIngredients ingredients);

	/**
	 * Get the tooltip for whatever's under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
	 * To add tooltips for a recipe wrapper, see {@link IRecipeWrapper#getTooltipStrings(int, int)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 * @since JEI 4.2.5
	 */
	default List<String> getTooltipStrings(int mouseX, int mouseY) {
		return Collections.emptyList();
	}
}
