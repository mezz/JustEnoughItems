package mezz.jei.api;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;

public interface IModPlugin {

	public Iterable<? extends IRecipeType> getRecipeTypes();
	public Iterable<? extends IRecipeHandler> getRecipeHandlers();
	public Iterable getRecipes();

}
