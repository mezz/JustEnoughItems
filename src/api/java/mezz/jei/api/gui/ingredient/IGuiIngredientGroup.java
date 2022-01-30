package mezz.jei.api.gui.ingredient;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;

/**
 * IGuiIngredientGroup displays recipe ingredients in a gui.
 *
 * If multiple ingredients are set for one index, they will be displayed in rotation.
 *
 * Get an instance from {@link IRecipeLayout}.
 *
 * @see IGuiItemStackGroup
 * @see IGuiFluidStackGroup
 *
 * @deprecated since JEI 9.3.0.
 * Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)}
 */
@Deprecated
public interface IGuiIngredientGroup<T> {
	/**
	 * Set a background image to draw behind the ingredient.
	 * Some examples are slot background or tank background.
	 */
	void setBackground(int slotIndex, IDrawable background);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 * @since JEI 9.3.0
	 */
	void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);

	/**
	 * Get the ingredients after they have been set.
	 * Used by recipe transfer handlers.
	 */
	Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients();

	/**
	 * Initialize a guiIngredient for the given slot.
	 * This can handle mod ingredients registered with {@link IModIngredientRegistration}.
	 *
	 * Uses the default {@link IIngredientRenderer} registered for the ingredient list in {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
	 * Uses the same 16x16 size as the ingredient list.
	 *
	 * For more advanced control over rendering, use {@link #init(int, boolean, IIngredientRenderer, int, int, int, int, int, int)}
	 *
	 * @param slotIndex the slot index of this ingredient
	 * @param input     whether this slot is an input
	 * @param xPosition x position relative to the recipe background
	 * @param yPosition y position relative to the recipe background
	 * @see IGuiItemStackGroup#init(int, boolean, int, int)
	 * @see IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)
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
	 * @param xInset             the extra x inner padding added to each side when drawing the ingredient
	 * @param yInset             the extra y inner padding added to each side when drawing the ingredient
	 * @see IGuiItemStackGroup#init(int, boolean, int, int)
	 * @see IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)
	 */
	void init(int slotIndex, boolean input,
			  IIngredientRenderer<T> ingredientRenderer,
			  int xPosition, int yPosition,
			  int width, int height,
			  int xInset, int yInset);
	void init(int slotIndex,
			  RecipeIngredientRole role,
			  IIngredientRenderer<T> ingredientRenderer,
			  int xPosition,
			  int yPosition,
			  int width,
			  int height,
			  int xInset,
			  int yInset);

	/**
	 * Force this ingredient group to display a different focus.
	 * This must be set before any ingredients are set.
	 *
	 * Useful for recipes that display things in a custom way depending on what the overall recipe focus is.
	 */
	void setOverrideDisplayFocus(@Nullable IFocus<T> focus);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 * @deprecated since JEI 9.3.0. Use {@link #addTooltipCallback(IRecipeSlotTooltipCallback)} instead.
	 */
	@Deprecated
	void addTooltipCallback(ITooltipCallback<T> tooltipCallback);

	/**
	 * Set all the ingredients in the group, based on the {@link IIngredients}
	 * passed to {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}.
	 *
	 * @deprecated since JEI 9.3.0.
	 * Instead of adding ingredients to {@link IGuiIngredientGroup},
	 * use {@link mezz.jei.api.gui.builder.IRecipeLayoutBuilder} from
	 * {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)} instead.
	 */
	@Deprecated
	void set(IIngredients ingredients);

	/**
	 * Set the ingredient at slotIndex to a rotating collection of ingredients.
	 *
	 * @deprecated since JEI 9.3.0.
	 * Instead of adding ingredients to {@link IGuiIngredientGroup},
	 * use {@link mezz.jei.api.gui.builder.IRecipeLayoutBuilder} from
	 * {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)} instead.
	 */
	@Deprecated
	void set(int slotIndex, @Nullable List<T> ingredients);

	/**
	 * Set the ingredient at slotIndex to a specific ingredient.
	 *
	 * @deprecated since JEI 9.3.0.
	 * Instead of adding ingredients to {@link IGuiIngredientGroup},
	 * use {@link mezz.jei.api.gui.builder.IRecipeLayoutBuilder} from
	 * {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)} instead.
	 */
	@Deprecated
	void set(int slotIndex, @Nullable T ingredient);
}
