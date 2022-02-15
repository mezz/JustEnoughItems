package mezz.jei.api.gui.ingredient;

import mezz.jei.api.recipe.IFocusGroup;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Ingredient;

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
 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
 * In the new system, this class is replaced by {@link IRecipeSlotBuilder},
 * which handles multiple ingredient types together.
 */
@Deprecated(forRemoval = true, since = "9.3.0")
public interface IGuiIngredientGroup<T> {

	/**
	 * Set all the ingredients in the group, based on the {@link IIngredients}
	 * passed to {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeSlotBuilder#addIngredients(Ingredient)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void set(IIngredients ingredients);

	/**
	 * Set the ingredient at ingredientIndex to a rotating collection of ingredients.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeSlotBuilder#addIngredients(IIngredientType, List)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void set(int ingredientIndex, @Nullable List<T> ingredients);

	/**
	 * Set the ingredient at ingredientIndex to a specific ingredient.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeSlotBuilder#addIngredient(IIngredientType, Object)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void set(int ingredientIndex, @Nullable T ingredient);

	/**
	 * Set a background image to draw behind the ingredient.
	 * Some examples are slot background or tank background.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeSlotBuilder#setBackground(IDrawable, int, int)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void setBackground(int ingredientIndex, IDrawable background);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void addTooltipCallback(ITooltipCallback<T> tooltipCallback);

	/**
	 * Get the ingredients after they have been set.
	 * Used by recipe transfer handlers.
	 *
	 * Note that this key index is related to the index of ingredients in {@link IIngredients},
	 * and not necessarily related to {@link Slot#index}.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this is replaced by {@link IRecipeSlotsView}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
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
	 * @param ingredientIndex the ingredient index of this ingredient
	 * @param input     whether this slot is an input
	 * @param xPosition x position relative to the recipe background
	 * @param yPosition y position relative to the recipe background
	 * @see IGuiItemStackGroup#init(int, boolean, int, int)
	 * @see IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by {@link IRecipeLayoutBuilder#addSlot(RecipeIngredientRole, int, int)}
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void init(int ingredientIndex, boolean input, int xPosition, int yPosition);

	/**
	 * Initialize a custom guiIngredient for the given slot.
	 * For default behavior, use the much simpler method {@link #init(int, boolean, int, int)}.
	 * For FluidStack, see {@link IGuiFluidStackGroup#init(int, boolean, int, int, int, int, int, boolean, IDrawable)}
	 * This can handle mod ingredients registered with {@link IModIngredientRegistration}.
	 *
	 * @param ingredientIndex    the ingredient index of this ingredient
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
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
	 * In the new system, this is replaced by
	 * {@link IRecipeLayoutBuilder#addSlot(RecipeIngredientRole, int, int)}
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void init(int ingredientIndex, boolean input,
			  IIngredientRenderer<T> ingredientRenderer,
			  int xPosition, int yPosition,
			  int width, int height,
			  int xInset, int yInset);

	/**
	 * Force this ingredient group to display a different focus.
	 * This must be set before any ingredients are set.
	 *
	 * Useful for recipes that display things in a custom way depending on what the overall recipe focus is.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * This is no longer available, you can directly limit the ingredients that are set.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void setOverrideDisplayFocus(@Nullable IFocus<T> focus);
}
