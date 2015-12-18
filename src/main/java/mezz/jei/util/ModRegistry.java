package mezz.jei.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import mezz.jei.RecipeRegistry;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipe;
import mezz.jei.transfer.BasicRecipeTransferHelper;

public class ModRegistry implements IModRegistry {
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
	private final List<IRecipeTransferHelper> recipeTransferHelpers = new ArrayList<>();
	private final List<Object> recipes = new ArrayList<>();

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
	public void addRecipes(List recipes) {
		if (recipes != null) {
			this.recipes.addAll(recipes);
		}
	}

	@Override
	public void addDescription(List<ItemStack> itemStacks, String... descriptionKeys) {
		if (itemStacks == null || itemStacks.size() == 0) {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.error("Tried to add description with no itemStacks.", e);
			return;
		}
		if (descriptionKeys.length == 0) {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.error("Tried to add an empty list of descriptionKeys for itemStacks {}.", itemStacks, e);
			return;
		}
		List<ItemDescriptionRecipe> recipes = ItemDescriptionRecipe.create(itemStacks, descriptionKeys);
		this.recipes.addAll(recipes);
	}

	@Override
	public void addIgnoredRecipeClasses(Class... ignoredRecipeClasses) {

	}

	public RecipeRegistry createRecipeRegistry() {
		return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHelpers, recipes);
	}
}
