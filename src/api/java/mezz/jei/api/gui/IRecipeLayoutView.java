package mezz.jei.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import javax.annotation.Nullable;

/**
 * Represents the layout of one recipe on-screen, updating in realtime.
 *
 * For use in {@link IRecipeCategory#draw(Object, IRecipeLayoutView, PoseStack, double, double)}
 */
public interface IRecipeLayoutView {
	@Nullable
	<T> T getDisplayedIngredient(IIngredientType<T> ingredientType, int slot);
}
