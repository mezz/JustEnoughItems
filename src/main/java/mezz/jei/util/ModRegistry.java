package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Container;

import mezz.jei.RecipeRegistry;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.transfer.BasicRecipeTransferHelper;

public class ModRegistry implements IModRegistry {
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
	private final List<IRecipeTransferHelper> recipeTransferHelpers = new ArrayList<>();
	private final List<Object> recipes = new ArrayList<>();
	private final List<Class> ignoredRecipeClasses = new ArrayList<>();

	@Override
	public void addRecipeCategories(IRecipeCategory... recipeCategories) {
		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipeHandlers(IRecipeHandler... recipeHandlers) {
		Collections.addAll(this.recipeHandlers, recipeHandlers);
	}

	@Override
	public void addRecipeTransferHelpers(IRecipeTransferHelper... recipeTransferHelpers) {
		Collections.addAll(this.recipeTransferHelpers, recipeTransferHelpers);
	}

	@Override
	public void addBasicRecipeTransferHelper(Class<? extends Container> containerClass, String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		IRecipeTransferHelper recipeTransferHelper = new BasicRecipeTransferHelper(containerClass, recipeCategoryUid, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		recipeTransferHelpers.add(recipeTransferHelper);
	}

	@Override
	public void addRecipes(@Nonnull List recipes) {
		this.recipes.addAll(recipes);
	}

	@Override
	public void addIgnoredRecipeClasses(Class... ignoredRecipeClasses) {
		Collections.addAll(this.ignoredRecipeClasses, ignoredRecipeClasses);
	}

	public RecipeRegistry createRecipeRegistry() {
		return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHelpers, recipes, ignoredRecipeClasses);
	}
}
