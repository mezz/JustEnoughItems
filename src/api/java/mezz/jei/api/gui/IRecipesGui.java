package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeRegistry;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link IFocus}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @see IRecipeRegistry#createFocus(IFocus.Mode, Object)
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
	Object getIngredientUnderMouse();
}
