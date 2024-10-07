package mezz.jei.api.runtime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

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
	Object getIngredientUnderMouse();

	/**
	 * Get the screen that the {@link IRecipesGui} was opened from.
	 * When the {@link IRecipesGui} is closed, it will re-open the parent screen.
	 *
	 * If the {@link IRecipesGui} is not open, this will return {@link Optional#empty()}.
	 *
	 * @since 7.19.0
	 */
	default Optional<Screen> getParentScreen() {
		return Optional.empty();
	}
}
