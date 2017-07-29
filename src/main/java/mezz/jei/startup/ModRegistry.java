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
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.plugins.jei.info.IngredientInfoRecipe;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.recipes.RecipeTransferRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class ModRegistry implements IModRegistry, IRecipeCategoryRegistration {
	private final IJeiHelpers jeiHelpers;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final Set<String> recipeCategoryUids = new HashSet<>();
	@Deprecated
	private final List<IRecipeHandler> unsortedRecipeHandlers = new ArrayList<>();
	private final Multimap<String, IRecipeHandler> recipeHandlers = ArrayListMultimap.create();
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<>();
	@Deprecated
	private final List<Object> unsortedRecipes = new ArrayList<>();
	private final Multimap<String, Object> recipes = ArrayListMultimap.create();
	private final RecipeTransferRegistry recipeTransferRegistry;
	private final Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = ArrayListMultimap.create();
	private final Multimap<String, Object> recipeCatalysts = ArrayListMultimap.create();
	private final List<IRecipeRegistryPlugin> recipeRegistryPlugins = new ArrayList<>();

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

	/**
	 * TODO: crash when there is no recipe category registered for recipeCategoryUid
	 * when {@link mezz.jei.api.IModRegistry#addRecipeCategories(IRecipeCategory[])} is removed
	 */
	@Override
	public void addRecipes(Collection<?> recipes, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipes, "recipes");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
//		Preconditions.checkArgument(this.recipeCategoryUids.contains(recipeCategoryUid), "No recipe category has been registered for recipeCategoryUid %s", recipeCategoryUid);
		if (!this.recipeCategoryUids.contains(recipeCategoryUid)) {
			Log.get().warn("No recipe category has been registered for recipeCategoryUid {}", recipeCategoryUid);
		}

		for (Object recipe : recipes) {
			ErrorUtil.checkNotNull(recipe, "recipe");
			this.recipes.put(recipeCategoryUid, recipe);
		}
	}

	@Override
	public <T> void handleRecipes(final Class<T> recipeClass, final IRecipeWrapperFactory<T> recipeWrapperFactory, final String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		Preconditions.checkArgument(!recipeClass.equals(Object.class), "Recipe handlers must handle a specific class, not Object.class");
		ErrorUtil.checkNotNull(recipeWrapperFactory, "recipeWrapperFactory");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

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
		ErrorUtil.checkNotNull(guiContainerClass, "guiContainerClass");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiContainerClass, recipeClickableArea);
	}

	@Override
	public void addRecipeCatalyst(Object catalystIngredient, String... recipeCategoryUids) {
		ErrorUtil.checkIsValidIngredient(catalystIngredient, "catalystIngredient");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		for (String recipeCategoryUid : recipeCategoryUids) {
			ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
			this.recipeCatalysts.put(recipeCategoryUid, catalystIngredient);
		}
	}

	@Override
	@Deprecated
	public void addRecipeCategoryCraftingItem(ItemStack craftingItem, String... recipeCategoryUids) {
		addRecipeCatalyst(craftingItem, recipeCategoryUids);
	}

	@Override
	public void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers) {
		ErrorUtil.checkNotEmpty(advancedGuiHandlers, "advancedGuiHandlers");

		Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
	}

	@Override
	@Deprecated
	public void addDescription(List<ItemStack> itemStacks, String... descriptionKeys) {
		addIngredientInfo(itemStacks, ItemStack.class, descriptionKeys);
	}

	@Override
	@Deprecated
	public void addDescription(ItemStack itemStack, String... descriptionKeys) {
		addIngredientInfo(itemStack, ItemStack.class, descriptionKeys);
	}

	@Override
	public <T> void addIngredientInfo(T ingredient, Class<? extends T> ingredientClass, String... descriptionKeys) {
		ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		addIngredientInfo(Collections.singletonList(ingredient), ingredientClass, descriptionKeys);
	}

	@Override
	public <T> void addIngredientInfo(List<T> ingredients, Class<? extends T> ingredientClass, String... descriptionKeys) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		for (Object ingredient : ingredients) {
			ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		}
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		List<IngredientInfoRecipe<T>> recipes = IngredientInfoRecipe.create(guiHelper, ingredients, ingredientClass, descriptionKeys);
		addRecipes(recipes, VanillaRecipeCategoryUid.INFORMATION);
	}

	@Override
	public void addAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
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
		ErrorUtil.checkNotNull(recipeRegistryPlugin, "recipeRegistryPlugin");

		Log.get().info("Added recipe registry plugin: {}", recipeRegistryPlugin.getClass());
		recipeRegistryPlugins.add(recipeRegistryPlugin);
	}

	public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
		return advancedGuiHandlers;
	}

	public RecipeRegistry createRecipeRegistry(IIngredientRegistry ingredientRegistry) {
		ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
		return new RecipeRegistry(recipeCategories, unsortedRecipeHandlers, recipeHandlers, recipeTransferHandlers, unsortedRecipes, recipes, recipeClickableAreas, recipeCatalysts, ingredientRegistry, recipeRegistryPlugins);
	}
}
