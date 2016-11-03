package mezz.jei.gui.recipes;

import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

public interface IRecipeGuiLogic {

	String getPageString();

	void setRecipesPerPage(int recipesPerPage);

	boolean hasMultipleCategories();

	boolean hasAllCategories();

	void previousRecipeCategory();

	void nextRecipeCategory();

	void setRecipeCategory(IRecipeCategory category);

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	<V> boolean setFocus(IFocus<V> focus);

	IFocus getFocus();

	boolean back();

	void clearHistory();

	boolean setCategoryFocus();

	boolean setCategoryFocus(List<String> recipeCategoryUids);

	IRecipeCategory getSelectedRecipeCategory();

	ImmutableList<IRecipeCategory> getRecipeCategories();

	List<ItemStack> getRecipeCategoryCraftingItems();

	List<ItemStack> getRecipeCategoryCraftingItems(IRecipeCategory recipeCategory);

	List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY);
}
