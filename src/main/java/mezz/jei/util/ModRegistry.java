package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;
import mezz.jei.JeiHelpers;
import mezz.jei.RecipeRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.plugins.jei.description.ItemDescriptionRecipe;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class ModRegistry implements IModRegistry {
	private final IJeiHelpers jeiHelpers;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<IRecipeHandler>();
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<IAdvancedGuiHandler<?>>();
	private final List<Object> recipes = new ArrayList<Object>();
	private final RecipeTransferRegistry recipeTransferRegistry;
	private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = HashMultimap.create();
	private final Multimap<String, ItemStack> craftItemsForCategories = ArrayListMultimap.create();
	private final List<IRecipeRegistryPlugin> recipeRegistryPlugins = new ArrayList<IRecipeRegistryPlugin>();

	public ModRegistry(JeiHelpers jeiHelpers, IIngredientRegistry ingredientRegistry) {
		this.jeiHelpers = jeiHelpers;
		this.ingredientRegistry = ingredientRegistry;
		this.recipeTransferRegistry = new RecipeTransferRegistry(jeiHelpers.getStackHelper(), jeiHelpers.recipeTransferHandlerHelper());
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IIngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}

	@Override
	public void addRecipeCategories(@Nullable IRecipeCategory... recipeCategories) {
		Preconditions.checkNotNull(recipeCategories, "recipeCategories cannot be null");
		Preconditions.checkArgument(recipeCategories.length > 0, "recipeCategories cannot be empty");

		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipeHandlers(@Nullable IRecipeHandler... recipeHandlers) {
		Preconditions.checkNotNull(recipeHandlers, "recipeHandlers cannot be null");
		Preconditions.checkArgument(recipeHandlers.length > 0, "recipeHandlers cannot be empty");

		Collections.addAll(this.recipeHandlers, recipeHandlers);
	}

	@Override
	public void addRecipes(@Nullable Collection recipes) {
		Preconditions.checkNotNull(recipes, "recipes cannot be null");
		Preconditions.checkArgument(!recipes.isEmpty(), "recipes cannot be empty");

		this.recipes.addAll(recipes);
	}

	@Override
	public void addRecipeClickArea(@Nullable Class<? extends GuiContainer> guiClass, int xPos, int yPos, int width, int height, @Nullable String... recipeCategoryUids) {
		Preconditions.checkNotNull(guiClass, "Tried to add a RecipeClickArea with null guiClass.");
		Preconditions.checkNotNull(recipeCategoryUids, "Tried to add a RecipeClickArea with null recipeCategoryUids.");
		Preconditions.checkArgument(recipeCategoryUids.length > 0, "Tried to add a RecipeClickArea with empty list of recipeCategoryUids.");

		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiClass, recipeClickableArea);
	}

	@Override
	public void addRecipeCategoryCraftingItem(@Nullable ItemStack craftingItem, @Nullable String... recipeCategoryUids) {
		Preconditions.checkNotNull(craftingItem, "Tried to add a RecipeCategoryCraftingItem with null craftingItem.");
		Preconditions.checkArgument(!craftingItem.isEmpty(), "Tried to add a RecipeCategoryCraftingItem with empty craftingItem.");
		Preconditions.checkNotNull(recipeCategoryUids, "Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUids.");
		Preconditions.checkArgument(recipeCategoryUids.length > 0, "Tried to add a RecipeCategoryCraftingItem with an empty list of recipeCategoryUids.");

		for (String recipeCategoryUid : recipeCategoryUids) {
			Preconditions.checkNotNull(recipeCategoryUid, "Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUid.");
			this.craftItemsForCategories.put(recipeCategoryUid, craftingItem);
		}
	}

	@Override
	public void addAdvancedGuiHandlers(@Nullable IAdvancedGuiHandler<?>... advancedGuiHandlers) {
		Preconditions.checkNotNull(advancedGuiHandlers, "advancedGuiHandlers cannot be null");
		Preconditions.checkArgument(advancedGuiHandlers.length > 0, "advancedGuiHandlers cannot be empty");

		Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
	}

	@Override
	public void addDescription(@Nullable List<ItemStack> itemStacks, @Nullable String... descriptionKeys) {
		Preconditions.checkNotNull(itemStacks, "Tried to add description with null itemStacks.");
		Preconditions.checkArgument(!itemStacks.isEmpty(), "Tried to add description with empty list of itemStacks.");
		Preconditions.checkNotNull(descriptionKeys, "Tried to add a null descriptionKey for itemStacks %s.", itemStacks);
		Preconditions.checkArgument(descriptionKeys.length > 0, "Tried to add an empty list of descriptionKeys for itemStacks %s.", itemStacks);

		for (ItemStack itemStack : itemStacks) {
			Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");
		}

		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		List<ItemDescriptionRecipe> recipes = ItemDescriptionRecipe.create(guiHelper, itemStacks, descriptionKeys);
		this.recipes.addAll(recipes);
	}

	@Override
	public void addDescription(@Nullable ItemStack itemStack, @Nullable String... descriptionKeys) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");
		Preconditions.checkNotNull(descriptionKeys, "descriptionKeys cannot be null");
		Preconditions.checkArgument(descriptionKeys.length > 0, "descriptionKeys cannot be empty");

		addDescription(Collections.singletonList(itemStack), descriptionKeys);
	}

	@Override
	public void addAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		Preconditions.checkNotNull(leftInput, "Tried to add an anvil recipe with a null leftInput");
		Preconditions.checkNotNull(rightInputs, "Tried to add an anvil recipe with a null rightInputs list");
		Preconditions.checkNotNull(outputs, "Tried to add an anvil recipe with a null outputs list");
		this.recipes.add(new AnvilRecipeWrapper(leftInput, rightInputs, outputs));
	}

	@Override
	public IRecipeTransferRegistry getRecipeTransferRegistry() {
		return recipeTransferRegistry;
	}

	@Override
	public void addRecipeRegistryPlugin(@Nullable IRecipeRegistryPlugin recipeRegistryPlugin) {
		Preconditions.checkNotNull(recipeRegistryPlugin, "recipeRegistryPlugin cannot be null");

		Log.info("Added recipe registry plugin: {}", recipeRegistryPlugin.getClass());
		recipeRegistryPlugins.add(recipeRegistryPlugin);
	}

	public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
		return advancedGuiHandlers;
	}

	public RecipeRegistry createRecipeRegistry(IIngredientRegistry ingredientRegistry) {
		ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
		return new RecipeRegistry(recipeCategories, recipeHandlers, recipeTransferHandlers, recipes, recipeClickableAreas, craftItemsForCategories, ingredientRegistry, recipeRegistryPlugins);
	}
}
