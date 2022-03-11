package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.recipe.IFocusGroup;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 * Get these from {@link IGuiIngredientGroup#getGuiIngredients()}.
 *
 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
 * In the new system, this class is replaced by {@link IRecipeSlotView},
 * which handles multiple ingredient types together.
 */
@Deprecated(forRemoval = true, since = "9.3.0")
public interface IGuiIngredient<T> {
	/**
	 * @return The ingredient type for this {@link IGuiIngredient}.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this class is replaced by {@link IRecipeSlotView}, which handles multiple ingredient types.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	IIngredientType<T> getIngredientType();

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this is replaced by {@link IRecipeSlotView#getDisplayedIngredient(IIngredientType)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	@Nullable
	T getDisplayedIngredient();

	/**
	 * All ingredient variations that can be shown.
	 * For ingredients that rotate through several values, this will have them all even if a focus is set.
	 * This list can contain null values.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this is replaced by {@link IRecipeSlotView#getAllIngredients()}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	List<T> getAllIngredients();

	/**
	 * Returns true if this ingredient is an input for the recipe, otherwise it is an output.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this is replaced by {@link IRecipeSlotView#getRole()}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	boolean isInput();

	/**
	 * Draws a highlight on background of this ingredient.
	 * This is used by recipe transfer errors to turn missing ingredient backgrounds to red, but can be used for other purposes.
	 *
	 * @see IRecipeTransferHandlerHelper#createUserErrorForMissingSlots(Component, Collection).
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
	 * In the new system, this is replaced by {@link IRecipeSlotView#drawHighlight(PoseStack, int)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset);
}
