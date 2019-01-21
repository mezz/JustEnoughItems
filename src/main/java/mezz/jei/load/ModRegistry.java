package mezz.jei.load;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.jei.info.IngredientInfoRecipe;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.recipes.RecipeTransferRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModRegistry implements IModRegistry, IRecipeCategoryRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IJeiHelpers jeiHelpers;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final Map<ResourceLocation, IRecipeCategory> recipeCategoriesByUid = new HashMap<>();
	private final Set<Class<?>> recipeClasses = new HashSet<>();
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers = new ArrayList<>();
	private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
	private final Map<Class, IGuiScreenHandler> guiScreenHandlers = new HashMap<>();
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = new HashMap<>();
	private final ListMultiMap<ResourceLocation, Object> recipes = new ListMultiMap<>();
	private final RecipeTransferRegistry recipeTransferRegistry;
	private final ListMultiMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas = new ListMultiMap<>();
	private final ListMultiMap<ResourceLocation, Object> recipeCatalysts = new ListMultiMap<>();
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
			ResourceLocation uid = recipeCategory.getUid();
			Preconditions.checkNotNull(uid, "Recipe category UID cannot be null %s", recipeCategory);
			Class<?> recipeClass = recipeCategory.getRecipeClass();
			Preconditions.checkNotNull(recipeClass, "Recipe class cannot be null %s", recipeCategory);
			if (!recipeClasses.add(recipeClass)) {
				throw new IllegalArgumentException("A RecipeCategory for recipe class \"" + recipeClass + "\" has already been registered.");
			}
			if (recipeCategoriesByUid.containsKey(uid)) {
				throw new IllegalArgumentException("A RecipeCategory with UID \"" + uid + "\" has already been registered.");
			} else {
				recipeCategoriesByUid.put(uid, recipeCategory);
			}
		}

		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipes(Collection<?> recipes, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipes, "recipes");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		IRecipeCategory recipeCategory = this.recipeCategoriesByUid.get(recipeCategoryUid);
		if (recipeCategory == null) {
		  throw new NullPointerException("No recipe category has been registered for recipeCategoryUid " + recipeCategoryUid);
		}
		Class<?> recipeClass = recipeCategory.getRecipeClass();

		for (Object recipe : recipes) {
			ErrorUtil.checkNotNull(recipe, "recipe");
			if (!recipeClass.isInstance(recipe)) {
			  throw new IllegalArgumentException(recipeCategory.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
			this.recipes.put(recipeCategoryUid, recipe);
		}
	}

	@Override
	public void addRecipeClickArea(Class<? extends GuiContainer> guiContainerClass, int xPos, int yPos, int width, int height, ResourceLocation... recipeCategoryUids) {
		ErrorUtil.checkNotNull(guiContainerClass, "guiContainerClass");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		RecipeClickableArea recipeClickableArea = new RecipeClickableArea(yPos, yPos + height, xPos, xPos + width, recipeCategoryUids);
		this.recipeClickableAreas.put(guiContainerClass, recipeClickableArea);
	}

	@Override
	public void addRecipeCatalyst(Object catalystIngredient, ResourceLocation... recipeCategoryUids) {
		ErrorUtil.checkIsValidIngredient(catalystIngredient, "catalystIngredient");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
			ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
			this.recipeCatalysts.put(recipeCategoryUid, catalystIngredient);
		}
	}

	@Override
	public void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers) {
		ErrorUtil.checkNotEmpty(advancedGuiHandlers, "advancedGuiHandlers");

		Collections.addAll(this.advancedGuiHandlers, advancedGuiHandlers);
	}

	@Override
	public void addGlobalGuiHandlers(IGlobalGuiHandler... globalGuiHandlers) {
		ErrorUtil.checkNotEmpty(globalGuiHandlers, "globalGuiHandlers");
		Collections.addAll(this.globalGuiHandlers, globalGuiHandlers);
	}

	@Override
	public <T extends GuiScreen> void addGuiScreenHandler(Class<T> guiClass, IGuiScreenHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
		Preconditions.checkArgument(!GuiScreen.class.equals(guiClass), "you cannot add a handler for GuiScreen, only a subclass.");
		ErrorUtil.checkNotNull(handler, "guiScreenHandler");
		this.guiScreenHandlers.put(guiClass, handler);
	}

	private static final List<Class<? extends GuiScreen>> ghostIngredientGuiBlacklist = ImmutableList.of(
		GuiScreen.class, GuiInventory.class, GuiContainerCreative.class
	);

	@Override
	public <T extends GuiScreen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
		Preconditions.checkArgument(!ghostIngredientGuiBlacklist.contains(guiClass), "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JEI: %s", ghostIngredientGuiBlacklist);
		ErrorUtil.checkNotNull(handler, "handler");
		this.ghostIngredientHandlers.put(guiClass, handler);
	}

	@Override
	public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, String... descriptionKeys) {
		ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		addIngredientInfo(Collections.singletonList(ingredient), ingredientType, descriptionKeys);
	}

	@Override
	public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		for (Object ingredient : ingredients) {
			ErrorUtil.checkIsValidIngredient(ingredient, "ingredient");
		}
		ErrorUtil.checkNotEmpty(descriptionKeys, "descriptionKeys");

		List<IngredientInfoRecipe<T>> recipes = IngredientInfoRecipe.create(ingredients, ingredientType, descriptionKeys);
		addRecipes(recipes, VanillaRecipeCategoryUid.INFORMATION);
	}

	@Override
	public IRecipeTransferRegistry getRecipeTransferRegistry() {
		return recipeTransferRegistry;
	}

	@Override
	public void addRecipeRegistryPlugin(IRecipeRegistryPlugin recipeRegistryPlugin) {
		ErrorUtil.checkNotNull(recipeRegistryPlugin, "recipeRegistryPlugin");

		LOGGER.info("Added recipe registry plugin: {}", recipeRegistryPlugin.getClass());
		recipeRegistryPlugins.add(recipeRegistryPlugin);
	}

	public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
		return advancedGuiHandlers;
	}

	public List<IGlobalGuiHandler> getGlobalGuiHandlers() {
		return globalGuiHandlers;
	}

	public Map<Class, IGuiScreenHandler> getGuiScreenHandlers() {
		return guiScreenHandlers;
	}

	public Map<Class, IGhostIngredientHandler> getGhostIngredientHandlers() {
		return ghostIngredientHandlers;
	}

	public RecipeRegistry createRecipeRegistry(IngredientRegistry ingredientRegistry) {
		ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers = recipeTransferRegistry.getRecipeTransferHandlers();
		return new RecipeRegistry(recipeCategories, recipeTransferHandlers, recipes, recipeClickableAreas, recipeCatalysts, ingredientRegistry, recipeRegistryPlugins);
	}
}
