package mezz.jei.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;
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
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.recipes.RecipeTransferRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class ModRegistry implements IModRegistry {
	private final IJeiHelpers jeiHelpers;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>();
	private final Set<String> recipeCategoryUids = new HashSet<String>();
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
	public void addRecipeCategories(IRecipeCategory... recipeCategories) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		for (IRecipeCategory recipeCategory : recipeCategories) {
			String uid = recipeCategory.getUid();
			Preconditions.checkNotNull(uid, "Recipe category UID cannot be null %s", recipeCategory);
			if (!recipeCategoryUids.add(uid)) {
				throw new IllegalArgumentException("A RecipeCategory with UID \"" + uid + "\" has already been registered.");
			}
		}

		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipeHandlers(IRecipeHandler... recipeHandlers) {
		ErrorUtil.checkNotEmpty(recipeHandlers, "recipeHandlers");

		for (IRecipeHandler recipeHandler : recipeHandlers) {
			Preconditions.checkNotNull(recipeHandler.getRecipeClass());
			Preconditions.checkArgument(!recipeHandler.getRecipeClass().equals(Object.class), "Recipe handlers must handle a specific class, not Object.class");
			this.recipeHandlers.add(recipeHandler);
		}
	}

	@Override
	public void addRecipes(Collection recipes) {
		ErrorUtil.checkNotEmpty(recipes, "recipes");

		this.recipes.addAll(recipes);
	}

	@Override
	public void addRecipeClickArea(Class<? extends GuiContainer> guiContainerClass, int xPos, int yPos, int width, int height, String... recipeCategoryUids) {
		Preconditions.checkNotNull(guiContainerClass, "Tried to add a RecipeClickArea with null guiContainerClass.");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiContainerClass, recipeClickableArea);
	}

	@Override
	public void addRecipeCategoryCraftingItem(ItemStack craftingItem, String... recipeCategoryUids) {
		ErrorUtil.checkNotEmpty(craftingItem);
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		for (String recipeCategoryUid : recipeCategoryUids) {
			Preconditions.checkNotNull(recipeCategoryUid, "Tried to add a RecipeCategoryCraftingItem with null recipeCategoryUid.");
			this.craftItemsForCategories.put(recipeCategoryUid, craftingItem);
		}
	}

	@Override
	public void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers) {
		ErrorUtil.checkNotEmpty(advancedGuiHandlers, "advancedGuiHandlers");

		Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
	}

	@Override
	public void addDescription(List<ItemStack> itemStacks, String... descriptionKeys) {
		ErrorUtil.checkNotEmpty(itemStacks, "itemStacks");
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		for (ItemStack itemStack : itemStacks) {
			ErrorUtil.checkNotEmpty(itemStack);
		}

		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		List<ItemDescriptionRecipe> recipes = ItemDescriptionRecipe.create(guiHelper, itemStacks, descriptionKeys);
		this.recipes.addAll(recipes);
	}

	@Override
	public void addDescription(ItemStack itemStack, String... descriptionKeys) {
		ErrorUtil.checkNotEmpty(itemStack);
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		addDescription(Collections.singletonList(itemStack), descriptionKeys);
	}

	@Override
	public void addAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput);
		ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");
		Preconditions.checkArgument(rightInputs.size() == outputs.size(), "Input and output sizes must match.");

		this.recipes.add(new AnvilRecipeWrapper(leftInput, rightInputs, outputs));
	}

	@Override
	public IRecipeTransferRegistry getRecipeTransferRegistry() {
		return recipeTransferRegistry;
	}

	@Override
	public void addRecipeRegistryPlugin(IRecipeRegistryPlugin recipeRegistryPlugin) {
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
