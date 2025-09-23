package mezz.jei.gui.config;

import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface ILookupHistoryConfig {
	void save(IRecipeManager recipeManager, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper, List<IBookmark> bookmarks);

	List<IBookmark> load(IRecipeManager recipeManager, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper);
}
