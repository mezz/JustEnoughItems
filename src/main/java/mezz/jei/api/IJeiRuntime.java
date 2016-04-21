package mezz.jei.api;

import javax.annotation.Nonnull;

/** Gives access to JEI functions that are available once everything has loaded */
public interface IJeiRuntime {
	@Nonnull
	IRecipeRegistry getRecipeRegistry();

	@Nonnull
	IItemListOverlay getItemListOverlay();

	/**
	 * @since JEI 3.2.12
	 */
	@Nonnull
	IRecipesGui getRecipesGui();
}
