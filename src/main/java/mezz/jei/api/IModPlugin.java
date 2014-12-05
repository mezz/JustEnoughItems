package mezz.jei.api;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;

public interface IModPlugin {

	public Iterable<? extends IRecipeCategory> getRecipeCategories();
	public Iterable<? extends IRecipeHandler> getRecipeHandlers();
	public Iterable<Object> getRecipes();

}
