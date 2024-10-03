package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IRecipeLayoutWithButtonsFactory {
	<T> IRecipeLayoutWithButtons<T> create(IRecipeLayoutDrawable<T> recipeLayoutDrawable, @Nullable RecipeBookmark<?, ?> recipeBookmark);

	default <T> IRecipeLayoutWithButtons<T> create(IRecipeLayoutDrawable<T> recipeLayoutDrawable) {
		return create(recipeLayoutDrawable, null);
	}
}
