package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * IGuiIngredientGroup displays recipe ingredients in a gui.
 * <p>
 * If multiple ingredients are set for one index, they will be displayed in rotation.
 * <p>
 * Get an instance from {@link IRecipeLayout}.
 *
 * @see IGuiItemStackGroup
 * @see IGuiFluidStackGroup
 */
public interface IGuiIngredientGroup<T> {

	/**
	 * Set all the ingredients in the group, based on the {@link IIngredients}
	 * passed to {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}.
	 *
	 * @since JEI 3.11.0
	 */
	void set(IIngredients ingredients);

	/**
	 * Set the ingredient at slotIndex to a rotating collection of ingredients.
	 *
	 * @since JEI 3.11.0
	 */
	void set(int slotIndex, @Nullable List<T> ingredients);

	/**
	 * Set the ingredient at slotIndex to a specific ingredient.
	 */
	void set(int slotIndex, @Nullable T ingredient);

	/**
	 * Set a background image to draw behind the ingredient.
	 * Some examples are slot background or tank background.
	 *
	 * @since JEI 4.3.1
	 */
	void setBackground(int slotIndex, IDrawable background);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 */
	void addTooltipCallback(ITooltipCallback<T> tooltipCallback);

	/**
	 * Get the ingredients after they have been set.
	 * Used by recipe transfer handlers.
	 */
	Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients();

	/**
	 * Initialize a guiIngredient for the given slot.
	 * This can handle mod ingredients registered with {@link IModIngredientRegistration}.
	 * <p>
	 * Uses the default {@link IIngredientRenderer} registered for the ingredient list in {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
	 * Uses the same 16x16 size as the ingredient list.
	 * <p>
	 * For more advanced control over rendering, use {@link #init(int, boolean, IIngredientRenderer, int, int, int, int, int, int)}
	 *
	 * @param slotIndex the slot index of this ingredient
	 * @param input     whether this slot is an input
	 * @param xPosition x position relative to the recipe background
	 * @param yPosition y position relative to the recipe background
	 * @see IGuiItemStackGroup#init(int, boolean, int, int)
	 * @see IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)
	 * @since JEI 4.0.2
	 */
	void init(int slotIndex, boolean input, int xPosition, int yPosition);

	/**
	 * Initialize a custom guiIngredient for the given slot.
	 * For default behavior, use the much simpler method {@link #init(int, boolean, int, int)}.
	 * For FluidStack, see {@link IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)}
	 * This can handle mod ingredients registered with {@link IModIngredientRegistration}.
	 *
	 * @param slotIndex          the slot index of this ingredient
	 * @param input              whether this slot is an input
	 * @param ingredientRenderer the ingredient renderer for this ingredient
	 * @param xPosition          x position relative to the recipe background
	 * @param yPosition          y position relative to the recipe background
	 * @param width              width of this ingredient
	 * @param height             height of this ingredient
	 * @param xPadding           the extra x padding added to each side when drawing the ingredient
	 * @param yPadding           the extra y padding added to each side when drawing the ingredient
	 * @see IGuiItemStackGroup#init(int, boolean, int, int)
	 * @see IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)
	 */
	void init(int slotIndex, boolean input,
			  IIngredientRenderer<T> ingredientRenderer,
			  int xPosition, int yPosition,
			  int width, int height,
			  int xPadding, int yPadding);

	/*
	 * Force this ingredient group to display a different focus.
	 * This must be set before any ingredients are set.
	 *
	 * Useful for recipes that display things in a custom way depending on what the overall recipe focus is.
	 *
	 * @since JEI 3.13.6
	 */
	void setOverrideDisplayFocus(@Nullable IFocus<T> focus);
}
