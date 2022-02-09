package mezz.jei.api.runtime;

import org.jetbrains.annotations.Nullable;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link IFocus}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @see IRecipeManager#createFocus(IFocus.Mode, Object)
	 */
	<V> void show(IFocus<V> focus);

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeCategoryUids a list of categories to display, in order. Must not be empty.
	 */
	void showCategories(List<ResourceLocation> recipeCategoryUids);

	/**
	 * @return the ingredient that's currently under the mouse in this gui, or null if there is none.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);
}
