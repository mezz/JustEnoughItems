package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.RecipeRegistry;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipe;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class ModRegistry implements IModRegistry {
	private final IJeiHelpers jeiHelpers;
	private final IItemRegistry itemRegistry;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<IRecipeHandler>();
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<IAdvancedGuiHandler<?>>();
	private final List<Object> recipes = new ArrayList<Object>();
	private final RecipeTransferRegistry recipeTransferRegistry = new RecipeTransferRegistry();
	private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = HashMultimap.create();
	private final Multimap<String, ItemStack> craftItemsForCategories = HashMultimap.create();

	public ModRegistry(IJeiHelpers jeiHelpers, IItemRegistry itemRegistry, IIngredientRegistry ingredientRegistry) {
		this.jeiHelpers = jeiHelpers;
		this.itemRegistry = itemRegistry;
		this.ingredientRegistry = ingredientRegistry;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IItemRegistry getItemRegistry() {
		return itemRegistry;
	}

	@Override
	public IIngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}

	@Override
	public void addRecipeCategories(@Nullable IRecipeCategory... recipeCategories) {
		if (recipeCategories != null) {
			Collections.addAll(this.recipeCategories, recipeCategories);
		}
	}

	@Override
	public void addRecipeHandlers(@Nullable IRecipeHandler... recipeHandlers) {
		if (recipeHandlers != null) {
			Collections.addAll(this.recipeHandlers, recipeHandlers);
		}
	}

	@Override
	public void addRecipes(@Nullable List recipes) {
		if (recipes != null) {
			this.recipes.addAll(recipes);
		}
	}

	@Override
	public void addRecipeClickArea(@Nullable Class<? extends GuiContainer> guiClass, int xPos, int yPos, int width, int height, @Nullable String... recipeCategoryUids) {
		if (guiClass == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeClickArea with null guiClass.", e);
			return;
		}

		if (recipeCategoryUids == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeClickArea with null recipeCategoryUids.", e);
			return;
		}

		if (recipeCategoryUids.length == 0) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeClickArea with empty list of recipeCategoryUids.", e);
			return;
		}
		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiClass, recipeClickableArea);
	}

	@Override
	public void addRecipeCategoryCraftingItem(@Nullable ItemStack craftingItem, @Nullable String... recipeCategoryUids) {
		if (craftingItem == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeCategoryCraftingItem with null craftingItem.", e);
			return;
		}

		if (craftingItem.getItem() == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeCategoryCraftingItem with null item in the craftingItem.", e);
			return;
		}

		if (recipeCategoryUids == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUids.", e);
			return;
		}

		if (recipeCategoryUids.length == 0) {
			NullPointerException e = new NullPointerException();
			Log.error("Tried to add a RecipeCategoryCraftingItem with an empty list of recipeCategoryUids.", e);
			return;
		}

		for (String recipeCategoryUid : recipeCategoryUids) {
			if (recipeCategoryUid == null) {
				IllegalArgumentException e = new IllegalArgumentException();
				Log.error("Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUid.", e);
			} else {
				this.craftItemsForCategories.put(recipeCategoryUid, craftingItem);
			}
		}
	}

	@Override
	public void addAdvancedGuiHandlers(@Nullable IAdvancedGuiHandler<?>... advancedGuiHandlers) {
		if (advancedGuiHandlers != null) {
			Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
		}
	}

	@Override
	public void addDescription(@Nullable List<ItemStack> itemStacks, @Nullable String... descriptionKeys) {
		if (itemStacks == null || itemStacks.size() == 0) {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.error("Tried to add description with no itemStacks.", e);
			return;
		}

		if (descriptionKeys == null) {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.error("Tried to add a null descriptionKey for itemStacks {}.", itemStacks, e);
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
	public void addDescription(@Nullable ItemStack itemStack, @Nullable String... descriptionKeys) {
		addDescription(Collections.singletonList(itemStack), descriptionKeys);
	}

	@Override
	public IRecipeTransferRegistry getRecipeTransferRegistry() {
		return recipeTransferRegistry;
	}

	public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
		return advancedGuiHandlers;
	}

	public RecipeRegistry createRecipeRegistry(IIngredientRegistry ingredientRegistry) {
		List<IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
		return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHandlers, recipes, recipeClickableAreas, craftItemsForCategories, ingredientRegistry);
	}
}
