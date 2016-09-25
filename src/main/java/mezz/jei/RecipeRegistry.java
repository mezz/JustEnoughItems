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
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Config;
import mezz.jei.gui.Focus;
import mezz.jei.gui.RecipeClickableArea;
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

public class RecipeRegistry implements IRecipeRegistry {
	private final IIngredientRegistry ingredientRegistry;
	private final List<IRecipeHandler> recipeHandlers;
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableMultimap<IRecipeCategory, ItemStack> craftItemsForCategories;
	private final ImmutableMultimap<String, String> categoriesForCraftItemKeys;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final ListMultimap<IRecipeCategory, Object> recipesForCategories;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses;

	public RecipeRegistry(
			List<IRecipeCategory> recipeCategories,
			List<IRecipeHandler> recipeHandlers,
			List<IRecipeTransferHandler> recipeTransferHandlers,
			List<Object> recipes,
			Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
			Multimap<String, ItemStack> craftItemsForCategories,
			IIngredientRegistry ingredientRegistry
	) {
		this.ingredientRegistry = ingredientRegistry;
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = buildRecipeTransferHandlerTable(recipeTransferHandlers);
		this.recipeHandlers = buildRecipeHandlersList(recipeHandlers);
		this.recipeClickableAreasMap = ImmutableMultimap.copyOf(recipeClickableAreasMap);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		this.unhandledRecipeClasses = new HashSet<Class>();

		this.recipesForCategories = ArrayListMultimap.create();
		addRecipes(recipes);

		StackHelper stackHelper = Internal.getStackHelper();

		ImmutableMultimap.Builder<IRecipeCategory, ItemStack> craftItemsForCategoriesBuilder = ImmutableMultimap.builder();
		ImmutableMultimap.Builder<String, String> categoriesForCraftItemKeysBuilder = ImmutableMultimap.builder();
		for (Map.Entry<String, Collection<ItemStack>> recipeCategoryEntry : craftItemsForCategories.asMap().entrySet()) {
			String recipeCategoryUid = recipeCategoryEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<ItemStack> craftItems = recipeCategoryEntry.getValue();
				craftItemsForCategoriesBuilder.putAll(recipeCategory, craftItems);
				for (ItemStack craftItem : craftItems) {
					recipeInputMap.addRecipeCategory(recipeCategory, craftItem);
					String craftItemKey = stackHelper.getUniqueIdentifierForStack(craftItem);
					categoriesForCraftItemKeysBuilder.put(craftItemKey, recipeCategoryUid);
				}
			}
		}

		this.craftItemsForCategories = craftItemsForCategoriesBuilder.build();
		this.categoriesForCraftItemKeys = categoriesForCraftItemKeysBuilder.build();
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

			Class recipeClass = recipeHandler.getRecipeClass();

			if (recipeHandlerClasses.contains(recipeClass)) {
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}

			recipeHandlerClasses.add(recipeClass);
			listBuilder.add(recipeHandler);
		}
		return listBuilder.build();
	}

	private static ImmutableTable<Class, String, IRecipeTransferHandler> buildRecipeTransferHandlerTable(List<IRecipeTransferHandler> recipeTransferHandlers) {
		ImmutableTable.Builder<Class, String, IRecipeTransferHandler> builder = ImmutableTable.builder();
		for (IRecipeTransferHandler recipeTransferHelper : recipeTransferHandlers) {
			builder.put(recipeTransferHelper.getContainerClass(), recipeTransferHelper.getRecipeCategoryUid(), recipeTransferHelper);
		}
		return builder.build();
	}

	private void addRecipes(@Nullable List<Object> recipes) {
		if (recipes == null) {
			return;
		}

		for (Object recipe : recipes) {
			addRecipe(recipe);
		}
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
		} catch (LinkageError ignored) {
			// older recipe wrappers do not support getIngredients
		}

		if (ingredients.isUsed()) {
			recipeInputMap.addRecipe(recipe, recipeCategory, ingredients.getInputIngredients());
			recipeOutputMap.addRecipe(recipe, recipeCategory, ingredients.getOutputIngredients());
		} else {
			legacy_addRecipeUnchecked(recipeWrapper, recipe, recipeCategory);
		}

		recipesForCategories.put(recipeCategory, recipe);
	}

	private <T> void legacy_addRecipeUnchecked(IRecipeWrapper recipeWrapper, T recipe, IRecipeCategory recipeCategory) {
		StackHelper stackHelper = Internal.getStackHelper();

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
			recipeInputMap.addRecipe(recipe, recipeCategory, inputIngredients);
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
			recipeOutputMap.addRecipe(recipe, recipeCategory, outputIngredients);
		}
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (IRecipeCategory recipeCategory : recipeCategoriesMap.values()) {
			if (!getRecipes(recipeCategory).isEmpty()) {
				builder.add(recipeCategory);
			}
		}
		return builder.build();
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nullable List<String> recipeCategoryUids) {
		if (recipeCategoryUids == null) {
			Log.error("Null recipeCategoryUids", new NullPointerException());
			return ImmutableList.of();
		}

		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (String recipeCategoryUid : recipeCategoryUids) {
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null && !getRecipes(recipeCategory).isEmpty()) {
				builder.add(recipeCategory);
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
	private static FluidStack getFluidFromItemBlock(Object ingredient) {
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
	public <V> ImmutableList<IRecipeCategory> getRecipeCategories(@Nullable IFocus<V> focus) {
		if (focus == null) {
			Log.error("Null focus", new NullPointerException());
			return ImmutableList.of();
		}

		if (focus.getMode() == IFocus.Mode.NONE) {
			return getRecipeCategories();
		}

		V ingredient = focus.getValue();
		if (ingredient == null) {
			Log.error("Null ingredient", new NullPointerException());
			return ImmutableList.of();
		}

		FluidStack fluidStack = getFluidFromItemBlock(ingredient);
		if (fluidStack != null) {
			return getRecipeCategories(createFocus(focus.getMode(), fluidStack));
		}

		switch (focus.getMode()) {
			case INPUT:
				return recipeInputMap.getRecipeCategories(ingredient);
			case OUTPUT:
				return recipeOutputMap.getRecipeCategories(ingredient);
			default:
				return getRecipeCategories();
		}
	}

	@Override
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

		FluidStack fluidStack = getFluidFromItemBlock(ingredient);
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
	@Deprecated
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable ItemStack input) {
		return getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable FluidStack input) {
		return getRecipeCategories(new Focus<FluidStack>(IFocus.Mode.INPUT, input));
	}

	@Override
	@Deprecated
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable ItemStack output) {
		return getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.OUTPUT, output));
	}

	@Override
	@Deprecated
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable FluidStack output) {
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
	public Collection<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, IFocus focus) {
		Collection<ItemStack> craftingItems = craftItemsForCategories.get(recipeCategory);
		Object ingredient = focus.getValue();
		if (ingredient instanceof ItemStack && focus.getMode() == IFocus.Mode.INPUT) {
			ItemStack itemStack = (ItemStack) ingredient;
			IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
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

		return recipeTransferHandlers.get(container.getClass(), recipeCategory.getUid());
	}
}
