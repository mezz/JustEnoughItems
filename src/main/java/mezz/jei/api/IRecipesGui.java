package mezz.jei.api;

import java.util.List;

import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 * Get the instance from {@link IJeiRuntime#getRecipesGui()}.
 *
 * @since JEI 3.2.12
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link IFocus}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @see IRecipeRegistry#createFocus(IFocus.Mode, Object)
	 * @since JEI 3.11.0
	 */
	<V> void show(IFocus<V> focus);

	/**
	 * Show recipes for an {@link ItemStack}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link ItemStack} result.
	 * @deprecated Since JEI 3.11.0. Use {@link #show(IFocus)}
	 */
	@Deprecated
	void showRecipes(ItemStack focus);

	/**
	 * Show recipes for a {@link FluidStack}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link FluidStack} result.
	 * @deprecated Since JEI 3.11.0. Use {@link #show(IFocus)}
	 */
	@Deprecated
	void showRecipes(FluidStack focus);

	/**
	 * Show recipes that use an {@link ItemStack} as an ingredient.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link ItemStack} ingredient.
	 * @deprecated Since JEI 3.11.0. Use {@link #show(IFocus)}
	 */
	@Deprecated
	void showUses(ItemStack focus);

	/**
	 * Show recipes that use a {@link FluidStack} as an ingredient.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link FluidStack} ingredient.
	 * @deprecated Since JEI 3.11.0. Use {@link #show(IFocus)}
	 */
	@Deprecated
	void showUses(FluidStack focus);

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeCategoryUids a list of categories to display, in order. Must not be empty.
	 */
	void showCategories(List<String> recipeCategoryUids);
}
