package mezz.jei.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Constants;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
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
	private final IIngredientRegistry ingredientRegistry;
	@Deprecated
	private final ImmutableList<IRecipeHandler> unsortedRecipeHandlers;
	private final ImmutableMultimap<String, IRecipeHandler> recipeHandlers;
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final Set<IRecipeCategory> emptyRecipeCategories = new HashSet<IRecipeCategory>();
	private final Set<IRecipeCategory> checkIfEmptyRecipeCategories = new HashSet<IRecipeCategory>();
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, ItemStack> craftItemsForCategories;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final Map<Object, IRecipeWrapper> wrapperMap = new IdentityHashMap<Object, IRecipeWrapper>(); // used when removing recipes
	private final ListMultimap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = ArrayListMultimap.create();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<IRecipeRegistryPlugin> plugins = new ArrayList<IRecipeRegistryPlugin>();

	public RecipeRegistry(
			List<IRecipeCategory> recipeCategories,
			List<IRecipeHandler> unsortedRecipeHandlers,
			Multimap<String, IRecipeHandler> recipeHandlers,
			ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers,
			List<Object> unsortedRecipes,
			Multimap<String, Object> recipes,
			Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
			Multimap<String, ItemStack> craftItemsForCategories,
			IIngredientRegistry ingredientRegistry,
			List<IRecipeRegistryPlugin> plugins
	) {
		this.ingredientRegistry = ingredientRegistry;
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);
		this.unsortedRecipeHandlers = buildRecipeHandlersList(unsortedRecipeHandlers);
		this.recipeClickableAreasMap = ImmutableMultimap.copyOf(recipeClickableAreasMap);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator, ingredientRegistry);

		addRecipes(unsortedRecipes, recipes);

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
		ImmutableMultimap<String, String> categoriesForCraftItemKeys = categoriesForCraftItemKeysBuilder.build();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForCraftItemKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipeWrappersForCategories);
		this.plugins.add(internalRecipeRegistryPlugin);
		this.plugins.addAll(plugins);

		for (IRecipeCategory recipeCategory : recipeCategories) {
			List recipeWrappers = getRecipeWrappers(recipeCategory);
			if (recipeWrappers.isEmpty()) {
				this.emptyRecipeCategories.add(recipeCategory);
			}
		}
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
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

	private static ImmutableMultimap<String, IRecipeHandler> buildRecipeHandlersMap(Multimap<String, IRecipeHandler> recipeHandlers) {
		ImmutableMultimap.Builder<String, IRecipeHandler> builder = ImmutableMultimap.builder();
		Multimap<String, Class> recipeHandlerClassesMap = ArrayListMultimap.create();
		for (Map.Entry<String, IRecipeHandler> entry : recipeHandlers.entries()) {
			IRecipeHandler recipeHandler = entry.getValue();
			Class recipeClass = recipeHandler.getRecipeClass();
			String recipeCategoryUid = entry.getKey();
			Collection<Class> recipeHandlerClasses = recipeHandlerClassesMap.get(recipeCategoryUid);
			if (!recipeHandlerClasses.contains(recipeClass)) {
				recipeHandlerClasses.add(recipeClass);
				builder.put(entry);
			} else {
				Log.error("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}
		}
		return builder.build();
	}

	private void addRecipes(List<Object> unsortedRecipes, Multimap<String, Object> recipes) {
		Collection<Map.Entry<String, Object>> entries = recipes.entries();
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Adding recipes", entries.size() + unsortedRecipes.size());
		for (Map.Entry<String, Object> entry : entries) {
			progressBar.step("");
			String recipeCategoryUid = entry.getKey();
			Object recipe = entry.getValue();
			addRecipe(recipe, recipeCategoryUid);
		}
		for (Object recipe : unsortedRecipes) {
			progressBar.step("");
			addRecipe(recipe);
		}
		ProgressManager.pop(progressBar);
	}

	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		return new Focus<V>(mode, ingredient);
	}

	@Override
	public void addRecipe(Object recipe) {
		Preconditions.checkNotNull(recipe, "recipe cannot be null");

		addRecipe(recipe, recipe.getClass(), null);
	}

	@Override
	public void addRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		Preconditions.checkNotNull(recipe, "recipe cannot be null");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid cannot be null");

		IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		addRecipe(recipe, recipe, recipeCategory);
	}

	private void addRecipe(Object recipe, String recipeCategoryUid) {
		Preconditions.checkNotNull(recipe, "recipe cannot be null");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid cannot be null");

		addRecipe(recipe, recipe.getClass(), recipeCategoryUid);
	}

	private <T> void addRecipe(T recipe, Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		if (recipeCategoryUid == null) {
			IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, null);
			if (recipeHandler != null) {
				recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			} else {
				Log.error("Could not determine recipe category for recipe: {}", recipeClass);
				return;
			}
		}

		IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeClass, recipeCategoryUid);
		if (recipeWrapper != null) {
			IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
			if (recipeCategory == null) {
				Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
				return;
			}

			addRecipe(recipe, recipeWrapper, recipeCategory);
		}
	}

	@Nullable
	private IRecipeCategory getRecipeCategory(String recipeCategoryUid) {
		return recipeCategoriesMap.get(recipeCategoryUid);
	}

	private static <T> void logBrokenRecipeHandler(T recipe, IRecipeHandler<T> recipeHandler) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e2) {
			Log.error("Failed recipe.toString", e2);
			recipeInfoBuilder.append(recipe.getClass());
		}
		recipeInfoBuilder.append("\nRecipe Handler failed to create recipe wrapper\n");
		recipeInfoBuilder.append(recipeHandler.getClass());
		Log.error(recipeInfoBuilder.toString());
	}

	private <T> void addRecipe(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		try {
			addRecipeUnchecked(recipe, recipeWrapper, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			Log.error("Found a broken crafting recipe.", e);
		} catch (RuntimeException e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.error("Found a broken recipe: {}\n", recipeInfo, e);
		} catch (LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	private <T> void addRecipeUnchecked(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		wrapperMap.put(recipe, recipeWrapper);

		Ingredients ingredients = getIngredients(recipeWrapper);

		recipeInputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getInputIngredients());
		recipeOutputMap.addRecipe(recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

		recipeWrappersForCategories.put(recipeCategory, recipeWrapper);

		if (emptyRecipeCategories.contains(recipeCategory)) {
			emptyRecipeCategories.remove(recipeCategory);
		}
	}

	public Ingredients getIngredients(IRecipeWrapper recipeWrapper) {
		Ingredients ingredients = new Ingredients();
		recipeWrapper.getIngredients(ingredients);
		return ingredients;
	}

	@Deprecated
	@Override
	public void removeRecipe(Object recipe) {
		Preconditions.checkNotNull(recipe, "Null recipe");

		removeRecipe(recipe, recipe.getClass(), null);
	}

	@Override
	public void removeRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		Preconditions.checkNotNull(recipe, "Null recipe");
		Preconditions.checkNotNull(recipeCategoryUid, "Null recipeCategoryUid");

		removeRecipe(recipe, recipe.getClass(), recipeCategoryUid);
	}

	private <T> void removeRecipe(T recipe, Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		if (recipeCategoryUid == null) {
			List<IRecipeHandler<T>> recipeHandlers = getRecipeHandlers(recipeClass);
			for (IRecipeHandler<T> recipeHandler : recipeHandlers) {
				removeRecipe(recipe, recipeHandler);
			}
		} else {
			IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, recipeCategoryUid);
			if (recipeHandler != null) {
				removeRecipe(recipe, recipeHandler);
			}
		}
	}

	private <T> void removeRecipe(T recipe, IRecipeHandler<T> recipeHandler) {
		String recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);

		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		try {
			removeRecipeUnchecked(recipe, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			Log.error("Found a broken crafting recipe.", e);
		} catch (RuntimeException e) {
			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.error("Found a broken recipe: {}\n", recipeInfo, e);
		} catch (LinkageError e) {
			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.error("Found a broken recipe: {}\n", recipeInfo, e);
		}
	}

	private <T> void removeRecipeUnchecked(T recipe, IRecipeCategory recipeCategory) {
		IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeCategory.getUid());
		if (recipeWrapper != null) {
			Ingredients ingredients = getIngredients(recipeWrapper);

			recipeInputMap.removeRecipe(recipeWrapper, recipeCategory, ingredients.getInputIngredients());
			recipeOutputMap.removeRecipe(recipeWrapper, recipeCategory, ingredients.getOutputIngredients());

			recipeWrappersForCategories.remove(recipeCategory, recipeWrapper);

			checkIfEmptyRecipeCategories.add(recipeCategory);
		}
	}

	@Override
	@Deprecated
	public void addSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		ErrorUtil.checkNotEmpty(inputs, "inputs");
		Preconditions.checkNotNull(output, "null output");

		SmeltingRecipe smeltingRecipe = new SmeltingRecipe(inputs, output);
		addRecipe(smeltingRecipe);
	}

	@Override
	public IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		ErrorUtil.checkNotEmpty(inputs, "inputs");
		Preconditions.checkNotNull(output);

		return new SmeltingRecipe(inputs, output);
	}

	@Override
	public IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput);
		ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");
		Preconditions.checkArgument(rightInputs.size() == outputs.size(), "Input and output sizes must match.");

		return new AnvilRecipeWrapper(leftInput, rightInputs, outputs);
	}

	@Override
	public List<IRecipeCategory> getRecipeCategories() {
		for (IRecipeCategory recipeCategory : this.checkIfEmptyRecipeCategories) {
			if (getRecipeWrappers(recipeCategory).isEmpty()) {
				this.emptyRecipeCategories.add(recipeCategory);
			}
		}
		this.checkIfEmptyRecipeCategories.clear();

		List<IRecipeCategory> recipeCategories = new ArrayList<IRecipeCategory>(this.recipeCategories);
		recipeCategories.removeAll(this.emptyRecipeCategories);
		return recipeCategories;
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids) {
		Preconditions.checkNotNull(recipeCategoryUids, "recipeCategoryUids cannot be null");

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

	@Deprecated
	@Nullable
	@Override
	public <T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass) {
		return getRecipeHandler(recipeClass, null);
	}

	@Nullable
	@Override
	public IRecipeWrapper getRecipeWrapper(Object recipe, String recipeCategoryUid) {
		return getRecipeWrapper(recipe, recipe.getClass(), recipeCategoryUid);
	}

	@Nullable
	private <T> IRecipeWrapper getRecipeWrapper(T recipe, Class<? extends T> recipeClass, String recipeCategoryUid) {
		if (wrapperMap.containsKey(recipe)) {
			return wrapperMap.get(recipe);
		}
		IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, recipeCategoryUid);
		if (recipeHandler != null) {
			try {
				if (!recipeHandler.isRecipeValid(recipe)) {
					return null;
				}
			} catch (RuntimeException e) {
				Log.error("Recipe check crashed", e);
				return null;
			} catch (LinkageError e) {
				Log.error("Recipe check crashed", e);
				return null;
			}

			try {
				IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
				wrapperMap.put(recipe, recipeWrapper);
				return recipeWrapper;
			} catch (RuntimeException e) {
				logBrokenRecipeHandler(recipe, recipeHandler);
				return null;
			} catch (LinkageError e) {
				logBrokenRecipeHandler(recipe, recipeHandler);
				return null;
			}
		} else if (recipe instanceof IRecipeWrapper) {
			return (IRecipeWrapper) recipe;
		} else {
			return null;
		}
	}

	@Nullable
	private <T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		Preconditions.checkNotNull(recipeClass, "recipeClass cannot be null");

		ImmutableCollection<IRecipeHandler> recipeHandlers;

		if (recipeCategoryUid != null) {
			recipeHandlers = this.recipeHandlers.get(recipeCategoryUid);
		} else {
			recipeHandlers = this.recipeHandlers.values();
		}

		// first try to find the exact handler for this recipeClass
		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
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
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		return null;
	}

	@Deprecated
	private <T> List<IRecipeHandler<T>> getRecipeHandlers(Class<? extends T> recipeClass) {
		Preconditions.checkNotNull(recipeClass, "recipeClass cannot be null");

		List<IRecipeHandler<T>> recipeHandlers = new ArrayList<IRecipeHandler<T>>();

		ImmutableCollection<IRecipeHandler> allRecipeHandlers = this.recipeHandlers.values();

		// first try to find the exact handler for this recipeClass
		for (IRecipeHandler<?> recipeHandler : allRecipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().equals(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}

		// fall back on any handler that can accept this recipeClass
		for (IRecipeHandler<?> recipeHandler : allRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}
		for (IRecipeHandler<?> recipeHandler : unsortedRecipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				recipeHandlers.add((IRecipeHandler<T>) recipeHandler);
			}
		}

		return recipeHandlers;
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

	public ImmutableCollection<RecipeClickableArea> getAllRecipeClickableAreas(GuiContainer gui) {
		return recipeClickableAreasMap.get(gui.getClass());
	}

	/**
	 * Special case for ItemBlocks containing fluid blocks.
	 * Nothing crafts those, the player probably wants to look up fluids.
	 */
	@Nullable
	private static FluidStack getFluidFromItemBlock(IFocus<?> focus) {
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
	public <V> List<IRecipeCategory> getRecipeCategories(IFocus<V> focus) {
		focus = Focus.check(focus);

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
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		Preconditions.checkNotNull(recipeCategory, "recipeCategory cannot be null");
		focus = Focus.check(focus);

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
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		Preconditions.checkNotNull(recipeCategory, "recipeCategory cannot be null");

		List<T> allRecipeWrappers = new ArrayList<T>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.warning("Recipe Wrapper lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
			}
			allRecipeWrappers.addAll(recipeWrappers);
		}

		return allRecipeWrappers;
	}

	@Override
	public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, @Nullable IFocus focus) {
		if (focus != null) {
			focus = Focus.check(focus);
		}
		List<ItemStack> craftingItems = craftItemsForCategories.get(recipeCategory);

		if (focus != null && focus.getMode() == IFocus.Mode.INPUT) {
			Object ingredient = focus.getValue();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				IIngredientHelper<ItemStack> ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
				ItemStack matchingStack = ingredientHelper.getMatch(craftingItems, itemStack);
				if (matchingStack != null) {
					return Collections.singletonList(matchingStack);
				}
			}
		}
		return craftingItems;
	}

	@Override
	public List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory) {
		Preconditions.checkNotNull(recipeCategory, "recipeCategory cannot be null");
		return craftItemsForCategories.get(recipeCategory);
	}

	@Override
	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory) {
		Preconditions.checkNotNull(container, "container cannot be null");
		Preconditions.checkNotNull(recipeCategory, "recipeCategory cannot be null");

		Class<? extends Container> containerClass = container.getClass();
		IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return recipeTransferHandler;
		}

		return recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}

	@Override
	public <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus focus) {
		focus = Focus.check(focus);
		return new RecipeLayout(-1, recipeCategory, recipeWrapper, focus, 0, 0);
	}
}
