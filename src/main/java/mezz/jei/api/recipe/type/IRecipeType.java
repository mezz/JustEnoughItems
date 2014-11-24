package mezz.jei.api.recipe.type;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeGui;

import javax.annotation.Nonnull;

/* Defines a type of recipe, i.e. Crafting Table Recipe, Furnace Recipe, etc. */
public interface IRecipeType {

	/* Returns the localized name for this recipe type. */
	@Nonnull
	String getLocalizedName();

	@Nonnull
	IDrawable getBackground();

	@Nonnull
	IRecipeGui createGui();

}
