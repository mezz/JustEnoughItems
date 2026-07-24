package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IFocusedRecipes<T> {
	IRecipeCategory<T> getRecipeCategory();

	@Unmodifiable
	List<T> getRecipes();
}
