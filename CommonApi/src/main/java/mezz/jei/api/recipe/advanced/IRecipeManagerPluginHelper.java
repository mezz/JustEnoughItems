package mezz.jei.api.recipe.advanced;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.types.IRecipeType;

/**
 * Helpers for implementing {@link IRecipeManagerPlugin}s.
 *
 * @since 19.15.1
 */
public interface IRecipeManagerPluginHelper {
	/**
	 * @return true if the given focus should be treated as a crafting station of this recipe type.
	 * @since 20.0.0
	 */
	boolean isCraftingStation(IRecipeType<?> recipeType, IFocus<?> focus);

	/**
	 * @return true if the given focus should be treated as a catalyst of this recipe type.
	 * @since 19.15.1
	 * @deprecated use {@link #isCraftingStation(IRecipeType, IFocus)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default boolean isRecipeCatalyst(IRecipeType<?> recipeType, IFocus<?> focus) {
		return isCraftingStation(recipeType, focus);
	}
}
