package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;

@FunctionalInterface
public interface IRecipeLayoutWithButtonsFactory {
	<T> IRecipeLayoutWithButtons<T> create(IRecipeLayoutDrawable<T> recipeLayoutDrawable);
}
