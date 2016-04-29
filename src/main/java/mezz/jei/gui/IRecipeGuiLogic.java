package mezz.jei.gui;

import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface IRecipeGuiLogic {

	@Nonnull
	String getPageString();

	void setRecipesPerPage(int recipesPerPage);

	boolean hasMultipleCategories();

	boolean hasAllCategories();

	void previousRecipeCategory();

	void nextRecipeCategory();

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	boolean setFocus(@Nonnull Focus focus);

	boolean back();

	void clearHistory();

	boolean setCategoryFocus();

	boolean setCategoryFocus(List<String> recipeCategoryUids);

	@Nullable
	Focus getFocus();

	@Nullable
	IRecipeCategory getRecipeCategory();

	@Nonnull
	Collection<ItemStack> getRecipeCategoryCraftingItems();

	@Nonnull
	List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY);
}
