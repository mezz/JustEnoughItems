package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IFocus;
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
	 * Add a callback to alter the tooltip for these ingredients.
	 */
	void addTooltipCallback(ITooltipCallback<T> tooltipCallback);

	/**
	 * Get the ingredients after they have been set.
	 * Used by recipe transfer handlers.
	 */
	Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients();

	/**
	 * Initialize a custom guiIngredient for the given slot.
	 * For ItemStacks and FluidStacks, use the much simpler methods in {@link IGuiItemStackGroup} and {@link IGuiFluidStackGroup}.
	 * This is for handling mod ingredients registered with {@link IModIngredientRegistration}.
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

	/**
	 * The current search focus. Set by the player when they look up the recipe. The object being looked up is the focus.
	 *
	 * @deprecated since JEI 3.11.0. Use {@link IRecipeLayout#getFocus()}
	 */
	@Deprecated
	IFocus<T> getFocus();

	/**
	 * Set the ingredient at slotIndex to a rotating collection of ingredients.
	 *
	 * @deprecated since JEI 3.11.0. Use {@link #set(int, List)}
	 */
	@Deprecated
	void set(int slotIndex, Collection<T> ingredients);
}
