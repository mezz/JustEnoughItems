package mezz.jei;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientUtil;
import mezz.jei.util.Ingredients;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeCategoryComparator;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ProgressManager;

public class RecipeRegistry implements IRecipeRegistry {
	private final StackHelper stackHelper;
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeHandler> recipeHandlers;
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, ItemStack> craftItemsForCategories;
	private final ImmutableMultimap<String, String> categoriesForCraftItemKeys;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final ListMultimap<IRecipeCategory, Object> recipesForCategories = ArrayListMultimap.create();
	private final ListMultimap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = ArrayListMultimap.create();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses = new HashSet<Class>();
	private final List<IRecipeRegistryPlugin> plugins = new ArrayList<IRecipeRegistryPlugin>();

	public RecipeRegistry(
			StackHelper stackHelper,
			List<IRecipeCategory> recipeCategories,
			List<IRecipeHandler> recipeHandlers,
			ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers,
			List<Object> recipes,
			Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
			Multimap<String, ItemStack> craftItemsForCategories,
			IIngredientRegistry ingredientRegistry,
			List<IRecipeRegistryPlugin> plugins
	) {
		this.stackHelper = stackHelper;
		this.ingredientRegistry = ingredientRegistry;
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeHandlers = buildRecipeHandlersList(recipeHandlers);
		this.recipeClickableAreasMap = ImmutableMultimap.copyOf(recipeClickableAreasMap);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		addRecipes(recipes);

		ImmutableListMultimap.Builder<IRecipeCategory, ItemStack> craftItemsForCategoriesBuilder = ImmutableListMultimap.builder();
		ImmutableMultimap.Builder<String, String> categoriesForCraftItemKeysBuilder = ImmutableMultimap.builder();

		IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
		for (Map.Entry<String, Collection<ItemStack>> recipeCategoryEntry : craftItemsForCategories.asMap().entrySet()) {
			String recipeCategoryUid = recipeCategoryEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<ItemStack> craftItems = recipeCategoryEntry.getValue();
				craftItemsForCategoriesBuilder.putAll(recipeCategory, craftItems);
				for (ItemStack craftItem : craftItems) {
					recipeInputMap.addRecipeCategory(recipeCategory, craftItem);
					String craftItemKey = ingredientHelper.getUniqueId(craftItem);
					categoriesForCraftItemKeysBuilder.put(craftItemKey, recipeCategoryUid);
				}
			}
		}

		this.craftItemsForCategories = craftItemsForCategoriesBuilder.build();
		this.categoriesForCraftItemKeys = categoriesForCraftItemKeysBuilder.build();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForCraftItemKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipeWrappersForCategories);
		this.plugins.add(internalRecipeRegistryPlugin);
		this.plugins.addAll(plugins);
	}

	private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private static ImmutableList<IRecipeHandler> buildRecipeHandlersList(List<IRecipeHandler> recipeHandlers) {
		ImmutableList.Builder<IRecipeHandler> listBuilder = ImmutableList.builder();
		Set<Class> recipeHandlerClasses = new HashSet<Class>();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass;
			try {
				recipeClass = recipeHandler.getRecipeClass();
			} catch (RuntimeException e) {
				Log.error("Recipe handler crashed.", e);
				continue;
			} catch (LinkageError e) {
				Log.error("Recipe handler crashed.", e);
				continue;
			}

			if (recipeHandlerClasses.contains(recipeClass)) {
				Log.error("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
				continue;
			}

			recipeHandlerClasses.add(recipeClass);
			listBuilder.add(recipeHandler);
		}
		return listBuilder.build();
	}

	private void addRecipes(@Nullable List<Object> recipes) {
		if (recipes == null) {
			return;
		}

		ProgressManager.ProgressBar progressBar = ProgressManager.push("Adding recipes", recipes.size());
		for (Object recipe : recipes) {
			progressBar.step("");
			addRecipe(recipe);
		}
		ProgressManager.pop(progressBar);
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, @Nullable V ingredient) {
		return new Focus<V>(mode, ingredient);
	}

	@Override
	public void addRecipe(@Nullable Object recipe) {
		if (recipe == null) {
			Log.error("Null recipe", new NullPointerException());
			return;
		}

		addRecipe(recipe, recipe.getClass());
	}

	private <T> void addRecipe(T recipe, Class<? extends T> recipeClass) {
		IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass);
		if (recipeHandler == null) {
			if (!unhandledRecipeClasses.contains(recipeClass)) {
				unhandledRecipeClasses.add(recipeClass);
				if (Config.isDebugModeEnabled()) {
					Log.debug("Can't handle recipe: {}", recipeClass);
				}
			}
			return;
		}

		String recipeCategoryUid;
		try {
			recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
		} catch (AbstractMethodError ignored) { // legacy handlers don't have that method
			recipeCategoryUid = recipeHandler.getRecipeCategoryUid();
		}

		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		if (!recipeHandler.isRecipeValid(recipe)) {
			return;
		}

		try {
			addRecipeUnchecked(recipe, recipeCategory, recipeHandler);
		} catch (RuntimeException e) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, recipeHandler);

			// suppress the null item in stack exception, that information is redundant here.
			String errorMessage = e.getMessage();
			if (StackHelper.nullItemInStack.equals(errorMessage)) {
				Log.error("Found a broken recipe: {}\n", recipeInfo);
			} else {
				Log.error("Found a broken recipe: {}\n", recipeInfo, e);
			}
		} catch (LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, recipeHandler);

			// suppress the null item in stack exception, that information is redundant here.
			String errorMessage = e.getMessage();
			if (StackHelper.nullItemInStack.equals(errorMessage)) {
				Log.error("Found a broken recipe: {}\n", recipeInfo);
			} else {
				Log.error("Found a broken recipe: {}\n", recipeInfo, e);
			}
		}
	}

	private <T> void addRecipeUnchecked(T recipe, IRecipeCategory recipeCategory, IRecipeHandler<T> recipeHandler) {
		IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

		Ingredients ingredients = new Ingredients();
		try {
			recipeWrapper.getIngredients(ingredients);
		} catch (AbstractMethodError ignored) {
			// older recipe wrappers do not support getIngredients
		}

		if (ingredients.isUsed()) {
			recipeInputMap.addRecipe(recipe, recipeCategory, recipeHandler, ingredients.getInputIngredients());
			recipeOutputMap.addRecipe(recipe, recipeCategory, recipeHandler, ingredients.getOutputIngredients());
		} else {
			legacy_addRecipeUnchecked(recipeWrapper, recipe, recipeHandler, recipeCategory);
		}

		recipesForCategories.put(recipeCategory, recipe);
		recipeWrappersForCategories.put(recipeCategory, recipeWrapper);
	}

	private <T> void legacy_addRecipeUnchecked(IRecipeWrapper recipeWrapper, T recipe, IRecipeHandler<T> recipeHandler, IRecipeCategory recipeCategory) {
		List inputs = recipeWrapper.getInputs();
		List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
		if (inputs != null || fluidInputs != null) {
			List<ItemStack> inputStacks = stackHelper.toItemStackList(inputs);
			if (fluidInputs == null) {
				fluidInputs = Collections.emptyList();
			}

			Map<Class, List> inputIngredients = new HashMap<Class, List>();
			inputIngredients.put(ItemStack.class, inputStacks);
			inputIngredients.put(FluidStack.class, fluidInputs);
			recipeInputMap.addRecipe(recipe, recipeCategory, recipeHandler, inputIngredients);
		}

		List outputs = recipeWrapper.getOutputs();
		List<FluidStack> fluidOutputs = recipeWrapper.getFluidOutputs();
		if (outputs != null || fluidOutputs != null) {
			List<ItemStack> outputStacks = stackHelper.toItemStackList(outputs);
			if (fluidOutputs == null) {
				fluidOutputs = Collections.emptyList();
			}

			Map<Class, List> outputIngredients = new HashMap<Class, List>();
			outputIngredients.put(ItemStack.class, outputStacks);
			outputIngredients.put(FluidStack.class, fluidOutputs);
			recipeOutputMap.addRecipe(recipe, recipeCategory, recipeHandler, outputIngredients);
		}
	}

	@Override
	public List<IRecipeCategory> getRecipeCategories() {
		return getRecipeCategories(createFocus(IFocus.Mode.NONE, null));
	}

	public int getRecipeCategoryCount() {
		return recipeCategoriesMap.size();
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nullable List<String> recipeCategoryUids) {
		if (recipeCategoryUids == null) {
			Log.error("Null recipeCategoryUids", new NullPointerException());
			return ImmutableList.of();
		}

		Set<String> uniqueUids = new HashSet<String>();
		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (String recipeCategoryUid : recipeCategoryUids) {
			if (!uniqueUids.contains(recipeCategoryUid)) {
				uniqueUids.add(recipeCategoryUid);
				IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
				if (recipeCategory != null && !getRecipeWrappers(recipeCategory).isEmpty()) {
					builder.add(recipeCategory);
				}
			}
		}
		return builder.build();
	}

	@Nullable
	@Override
	public <T> IRecipeHandler<T> getRecipeHandler(@Nullable Class<? extends T> recipeClass) {
		if (recipeClass == null) {
			Log.error("Null recipeClass", new NullPointerException());
			return null;
		}

		// first try to find the exact handler for this recipeClass
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		// fall back on any handler that can accept this recipeClass
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		return null;
	}

	@Nullable
	public RecipeClickableArea getRecipeClickableArea(GuiContainer gui, int mouseX, int mouseY) {
		ImmutableCollection<RecipeClickableArea> recipeClickableAreas = recipeClickableAreasMap.get(gui.getClass());
		for (RecipeClickableArea recipeClickableArea : recipeClickableAreas) {
			if (recipeClickableArea.checkHover(mouseX, mouseY)) {
				return recipeClickableArea;
			}
		}
		return null;
	}

	/**
	 * Special case for ItemBlocks containing fluid blocks.
	 * Nothing crafts those, the player probably wants to look up fluids.
	 */
	@Nullable
	private static FluidStack getFluidFromItemBlock(IFocus<?> focus) {
		if (focus.getMode() == IFocus.Mode.NONE) {
			return null;
		}

		Object ingredient = focus.getValue();
		if (ingredient instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) ingredient;
			Item item = itemStack.getItem();
			if (item instanceof ItemBlock) {
				Block block = ((ItemBlock) item).getBlock();
				Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
				if (fluid != null) {
					return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
				}
			}
		}

		return null;
	}

	@Override
	public <V> List<IRecipeCategory> getRecipeCategories(@Nullable IFocus<V> focus) {
		if (focus == null) {
			Log.error("Null focus", new NullPointerException());
			return ImmutableList.of();
		}

		FluidStack fluidStack = getFluidFromItemBlock(focus);
		if (fluidStack != null) {
			return getRecipeCategories(createFocus(focus.getMode(), fluidStack));
		}

		List<String> allRecipeCategoryUids = new ArrayList<String>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<String> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.warning("Recipe Category lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
			}
			allRecipeCategoryUids.addAll(recipeCategoryUids);
		}

		return getRecipeCategories(allRecipeCategoryUids);
	}

	@Override
	@Deprecated
	public <V> List<Object> getRecipes(@Nullable IRecipeCategory recipeCategory, @Nullable IFocus<V> focus) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		}

		if (focus == null) {
			Log.error("Null focus", new NullPointerException());
			return ImmutableList.of();
		}

		if (focus.getMode() == IFocus.Mode.NONE) {
			return getRecipes(recipeCategory);
		}

		V ingredient = focus.getValue();
		if (ingredient == null) {
			Log.error("Null ingredient", new NullPointerException());
			return ImmutableList.of();
		}

		FluidStack fluidStack = getFluidFromItemBlock(focus);
		if (fluidStack != null) {
			return getRecipes(recipeCategory, createFocus(focus.getMode(), fluidStack));
		}

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		if (focus.getMode() == IFocus.Mode.INPUT) {
			final ImmutableList<Object> recipes = recipeInputMap.getRecipes(recipeCategory, ingredient);

			String recipeCategoryUid = recipeCategory.getUid();
			for (String inputKey : IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
				if (categoriesForCraftItemKeys.get(inputKey).contains(recipeCategoryUid)) {
					ImmutableSet<Object> specificRecipes = ImmutableSet.copyOf(recipes);
					List<Object> recipesForCategory = recipesForCategories.get(recipeCategory);
					List<Object> allRecipes = new ArrayList<Object>(recipes);
					for (Object recipe : recipesForCategory) {
						if (!specificRecipes.contains(recipe)) {
							allRecipes.add(recipe);
						}
					}
					return allRecipes;
				}
			}

			return recipes;
		} else if (focus.getMode() == IFocus.Mode.OUTPUT) {
			return recipeOutputMap.getRecipes(recipeCategory, ingredient);
		} else {
			return getRecipes(recipeCategory);
		}
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(@Nullable IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		}

		if (focus == null) {
			Log.error("Null focus", new NullPointerException());
			return ImmutableList.of();
		}

		FluidStack fluidStack = getFluidFromItemBlock(focus);
		if (fluidStack != null) {
			return getRecipeWrappers(recipeCategory, createFocus(focus.getMode(), fluidStack));
		}

		List<T> allRecipeWrappers = new ArrayList<T>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory, focus);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.warning("Recipe Wrapper lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
			}
			allRecipeWrappers.addAll(recipeWrappers);
		}

		return allRecipeWrappers;
	}

	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(@Nullable IRecipeCategory<T> recipeCategory) {
		return getRecipeWrappers(recipeCategory, createFocus(IFocus.Mode.NONE, null));
	}

	@Override
	@Deprecated
	public List<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable ItemStack input) {
		return getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public List<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable FluidStack input) {
		return getRecipeCategories(new Focus<FluidStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public List<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable ItemStack output) {
		return getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.OUTPUT, output));
	}

	@Override
	@Deprecated
	public List<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable FluidStack output) {
		return getRecipeCategories(new Focus<FluidStack>(IFocus.Mode.OUTPUT, output));
	}

	@Override
	@Deprecated
	public List<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack input) {
		return getRecipes(recipeCategory, new Focus<ItemStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public List<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable FluidStack input) {
		return getRecipes(recipeCategory, new Focus<FluidStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public List<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack output) {
		return getRecipes(recipeCategory, new Focus<ItemStack>(IFocus.Mode.OUTPUT, output));
	}

	@Override
	@Deprecated
	public List<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable FluidStack output) {
		return getRecipes(recipeCategory, new Focus<FluidStack>(IFocus.Mode.OUTPUT, output));
	}

	@Override
	@Deprecated
	public List<Object> getRecipes(@Nullable IRecipeCategory recipeCategory) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		}
		return Collections.unmodifiableList(recipesForCategories.get(recipeCategory));
	}

	@Override
	@Deprecated
	public ImmutableCollection<ItemStack> getCraftingItems(IRecipeCategory recipeCategory) {
		return craftItemsForCategories.get(recipeCategory);
	}

	@Override
	public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, IFocus focus) {
		List<ItemStack> craftingItems = craftItemsForCategories.get(recipeCategory);
		Object ingredient = focus.getValue();
		if (ingredient instanceof ItemStack && focus.getMode() == IFocus.Mode.INPUT) {
			ItemStack itemStack = (ItemStack) ingredient;
			IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
			ItemStack matchingStack = ingredientHelper.getMatch(craftingItems, itemStack);
			if (matchingStack != null) {
				return Collections.singletonList(matchingStack);
			}
		}
		return craftingItems;
	}

	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(@Nullable Container container, @Nullable IRecipeCategory recipeCategory) {
		if (container == null) {
			Log.error("Null container", new NullPointerException());
			return null;
		} else if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return null;
		}

		Class<? extends Container> containerClass = container.getClass();
		IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return recipeTransferHandler;
		}

		return recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}

}
