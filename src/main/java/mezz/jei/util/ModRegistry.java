package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import mezz.jei.RecipeRegistry;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipe;

public class ModRegistry implements IModRegistry {
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
	private final List<Object> recipes = new ArrayList<>();
	private final RecipeTransferRegistry recipeTransferRegistry = new RecipeTransferRegistry();
	private final Map<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = new HashMap<>();

	@Override
	public void addRecipeCategories(IRecipeCategory... recipeCategories) {
		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipeHandlers(IRecipeHandler... recipeHandlers) {
		Collections.addAll(this.recipeHandlers, recipeHandlers);
	}

	@Override
	public void addRecipes(List recipes) {
		if (recipes != null) {
			this.recipes.addAll(recipes);
		}
	}

	@Override
	public void addRecipeClickArea(@Nonnull Class<? extends GuiContainer> guiClass, int xPos, int yPos, int width, int height, @Nonnull String... recipeCategoryUids) {
		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiClass, recipeClickableArea);
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
	public void addDescription(ItemStack itemStack, String... descriptionKeys) {
		addDescription(Collections.singletonList(itemStack), descriptionKeys);
	}

	@Override
	public IRecipeTransferRegistry getRecipeTransferRegistry() {
		return recipeTransferRegistry;
	}

	@Nonnull
	public RecipeRegistry createRecipeRegistry() {
		List<IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
		return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHandlers, recipes, recipeClickableAreas);
	}
}
