package mezz.jei.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
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
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
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
	@Deprecated
	private final List<IRecipeHandler> unsortedRecipeHandlers = new ArrayList<IRecipeHandler>();
	private final Multimap<String, IRecipeHandler> recipeHandlers = ArrayListMultimap.create();
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<IAdvancedGuiHandler<?>>();
	@Deprecated
	private final List<Object> unsortedRecipes = new ArrayList<Object>();
	private final Multimap<String, Object> recipes = ArrayListMultimap.create();
	private final RecipeTransferRegistry recipeTransferRegistry;
	private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = ArrayListMultimap.create();
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
	@Deprecated
	public void addRecipeHandlers(IRecipeHandler... recipeHandlers) {
		ErrorUtil.checkNotEmpty(recipeHandlers, "recipeHandlers");

		for (IRecipeHandler recipeHandler : recipeHandlers) {
			Preconditions.checkNotNull(recipeHandler.getRecipeClass());
			Preconditions.checkArgument(!recipeHandler.getRecipeClass().equals(Object.class), "Recipe handlers must handle a specific class, not Object.class");
			this.unsortedRecipeHandlers.add(recipeHandler);
		}
	}

	@Override
	@Deprecated
	public void addRecipes(Collection recipes) {
		ErrorUtil.checkNotNull(recipes, "recipes");

		this.unsortedRecipes.addAll(recipes);
	}

	@Override
	public void addRecipes(Collection<?> recipes, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipes, "recipes");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid must not be null");
		//TODO : add this without breaking description recipes
//		Preconditions.checkArgument(this.recipeCategoryUids.contains(recipeCategoryUid), "No recipe category has been registered for recipeCategoryUid %s", recipeCategoryUid);

		for (Object recipe : recipes) {
			Preconditions.checkNotNull(recipe, "recipe must not be null");
			this.recipes.put(recipeCategoryUid, recipe);
		}
	}

	@Override
	public <T> void handleRecipes(final Class<T> recipeClass, final IRecipeWrapperFactory<T> recipeWrapperFactory, final String recipeCategoryUid) {
		Preconditions.checkNotNull(recipeClass, "recipeClass cannot be null");
		Preconditions.checkArgument(!recipeClass.equals(Object.class), "Recipe handlers must handle a specific class, not Object.class");
		Preconditions.checkNotNull(recipeWrapperFactory, "recipeWrapperFactory cannot be null");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid cannot be null");

		IRecipeHandler<T> recipeHandler = new IRecipeHandler<T>() {
			@Override
			public Class<T> getRecipeClass() {
				return recipeClass;
			}

			@Override
			public String getRecipeCategoryUid(T recipe) {
				return recipeCategoryUid;
			}

			@Override
			public IRecipeWrapper getRecipeWrapper(T recipe) {
				return recipeWrapperFactory.getRecipeWrapper(recipe);
			}

			@Override
			public boolean isRecipeValid(T recipe) {
				return true;
			}
		};

		this.recipeHandlers.put(recipeCategoryUid, recipeHandler);
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
		addRecipes(recipes, VanillaRecipeCategoryUid.DESCRIPTION);
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

		AnvilRecipeWrapper anvilRecipeWrapper = new AnvilRecipeWrapper(leftInput, rightInputs, outputs);
		addRecipes(Collections.singletonList(anvilRecipeWrapper), VanillaRecipeCategoryUid.ANVIL);
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
		return new RecipeRegistry(recipeCategories, unsortedRecipeHandlers, recipeHandlers, recipeTransferHandlers, unsortedRecipes, recipes, recipeClickableAreas, craftItemsForCategories, ingredientRegistry, recipeRegistryPlugins);
	}
}
