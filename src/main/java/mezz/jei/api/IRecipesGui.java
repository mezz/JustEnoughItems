package mezz.jei.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI's gui for displaying recipes. Use this interface to open recipes.
 *
 * @since JEI 3.2.12
 */
public interface IRecipesGui {
	/**
	 * Show recipes for an {@link ItemStack}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link ItemStack} result.
	 */
	void showRecipes(@Nonnull ItemStack focus);

	/**
	 * Show recipes for a {@link Fluid}.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link Fluid} result.
	 */
	void showRecipes(@Nonnull Fluid focus);

	/**
	 * Show recipes that use an {@link ItemStack} as an ingredient.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link ItemStack} ingredient.
	 */
	void showUses(@Nonnull ItemStack focus);

	/**
	 * Show recipes that use a {@link Fluid} as an ingredient.
	 * Opens the {@link IRecipesGui} if it is closed.
	 *
	 * @param focus the {@link Fluid} ingredient.
	 */
	void showUses(@Nonnull Fluid focus);

	/**
	 * Show entire categories of recipes.
	 *
	 * @param recipeCategoryUids a list of categories to display, in order. Must not be empty.
	 */
	void showCategories(@Nonnull List<String> recipeCategoryUids);
}
