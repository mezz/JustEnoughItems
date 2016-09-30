package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.List;

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

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	boolean setFocus(IFocus focus);

	@Nullable
	IFocus getFocus();

	boolean back();

	void clearHistory();

	boolean setCategoryFocus();

	boolean setCategoryFocus(List<String> recipeCategoryUids);

	@Nullable
	IRecipeCategory getRecipeCategory();

	List<ItemStack> getRecipeCategoryCraftingItems();

	List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY);
}
