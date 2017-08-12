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
import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Constants;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.Ingredients;
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
	private final Set<IRecipeCategory> emptyRecipeCategories = new HashSet<>();
	private final Set<IRecipeCategory> checkIfEmptyRecipeCategories = new HashSet<>();
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableListMultimap<IRecipeCategory, Object> recipeCatalysts;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final Map<Object, IRecipeWrapper> wrapperMap = new IdentityHashMap<>(); // used when removing recipes
	private final ListMultimap<IRecipeCategory, IRecipeWrapper> recipeWrappersForCategories = ArrayListMultimap.create();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final List<IRecipeRegistryPlugin> plugins = new ArrayList<>();

	public RecipeRegistry(
			List<IRecipeCategory> recipeCategories,
			List<IRecipeHandler> unsortedRecipeHandlers,
			Multimap<String, IRecipeHandler> recipeHandlers,
			ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers,
			List<Object> unsortedRecipes,
			Multimap<String, Object> recipes,
			Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
			Multimap<String, Object> recipeCatalysts,
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

		ImmutableListMultimap.Builder<IRecipeCategory, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
		ImmutableMultimap.Builder<String, String> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();

		for (Map.Entry<String, Collection<Object>> recipeCatalystEntry : recipeCatalysts.asMap().entrySet()) {
			String recipeCategoryUid = recipeCatalystEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<Object> catalystIngredients = recipeCatalystEntry.getValue();
				recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
				for (Object catalystIngredient : catalystIngredients) {
					recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient);
					String catalystIngredientKey = getUniqueId(catalystIngredient);
					categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategoryUid);
				}
			}
		}

		this.recipeCatalysts = recipeCatalystsBuilder.build();
		ImmutableMultimap<String, String> categoriesForRecipeCatalystKeys = categoriesForRecipeCatalystKeysBuilder.build();

		IRecipeRegistryPlugin internalRecipeRegistryPlugin = new InternalRecipeRegistryPlugin(this, categoriesForRecipeCatalystKeys, ingredientRegistry, recipeCategoriesMap, recipeInputMap, recipeOutputMap, recipeWrappersForCategories);
		this.plugins.add(internalRecipeRegistryPlugin);
		this.plugins.addAll(plugins);

		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			List recipeWrappers = getRecipeWrappers(recipeCategory);
			if (recipeWrappers.isEmpty()) {
				this.emptyRecipeCategories.add(recipeCategory);
			}
		}
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
	}

	private <T> String getUniqueId(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient);
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
		Set<Class> recipeHandlerClasses = new HashSet<>();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass;
			try {
				recipeClass = recipeHandler.getRecipeClass();
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Recipe handler crashed.", e);
				continue;
			}

			if (recipeHandlerClasses.contains(recipeClass)) {
				Log.get().error("A Recipe Handler has already been registered for this recipe class: {}", recipeClass.getName());
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
				Log.get().error("A Recipe Handler has already been registered for this recipe class: {}", recipeClass.getName());
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
		return new Focus<>(mode, ingredient);
	}

	@Override
	public void addRecipe(Object recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");

		addRecipe(recipe, recipe.getClass(), null);
	}

	@Override
	public void addRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		addRecipe(recipe, recipe, recipeCategory);
	}

	private void addRecipe(Object recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		addRecipe(recipe, recipe.getClass(), recipeCategoryUid);
	}

	private <T> void addRecipe(T recipe, Class<? extends T> recipeClass, @Nullable String recipeCategoryUid) {
		if (recipeCategoryUid == null) {
			IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass, null);
			if (recipeHandler != null) {
				recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			} else {
				Log.get().error("Could not determine recipe category for recipe: {}", recipeClass);
				return;
			}
		}

		IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe, recipeClass, recipeCategoryUid);
		if (recipeWrapper != null) {
			IRecipeCategory recipeCategory = getRecipeCategory(recipeCategoryUid);
			if (recipeCategory == null) {
				Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
				return;
			}

			addRecipe(recipe, recipeWrapper, recipeCategory);
		} else {
			Log.get().debug("No recipe wrapper for recipe: {}", recipeClass);
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
			Log.get().error("Failed recipe.toString", e2);
			recipeInfoBuilder.append(recipe.getClass());
		}
		recipeInfoBuilder.append("\nRecipe Handler failed to create recipe wrapper\n");
		recipeInfoBuilder.append(recipeHandler.getClass());
		Log.get().error("{}", recipeInfoBuilder.toString());
	}

	private <T> void addRecipe(T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory) {
		try {
			addRecipeUnchecked(recipe, recipeWrapper, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			Log.get().error("Found a broken crafting recipe.", e);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
			Log.get().error("Found a broken recipe: {}\n", recipeInfo, e);
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
		ErrorUtil.checkNotNull(recipe, "recipe");

		List<IRecipeHandler<Object>> recipeHandlers1 = getRecipeHandlers(recipe.getClass());
		for (IRecipeHandler<Object> recipeHandler : recipeHandlers1) {
			String recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
			removeRecipe(recipe, recipeCategoryUid);
		}
	}

	@Override
	public void removeRecipe(IRecipeWrapper recipe, String recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		removeRecipe((Object) recipe, recipeCategoryUid);
	}

	private <T> void removeRecipe(T recipe, String recipeCategoryUid) {
		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.get().error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		try {
			removeRecipeUnchecked(recipe, recipeCategory);
		} catch (BrokenCraftingRecipeException e) {
			Log.get().error("Found a broken crafting recipe.", e);
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
		ErrorUtil.checkNotEmpty(output, "output");

		SmeltingRecipe smeltingRecipe = new SmeltingRecipe(inputs, output);
		addRecipe(smeltingRecipe);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		return Internal.getHelpers().getVanillaRecipeFactory().createSmeltingRecipe(inputs, output);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		return Internal.getHelpers().getVanillaRecipeFactory().createAnvilRecipe(leftInput, rightInputs, outputs);
	}

	@Override
	@Deprecated
	public IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		return Internal.getHelpers().getVanillaRecipeFactory().createBrewingRecipe(ingredients, potionInput, potionOutput);
	}

	@Override
	public List<IRecipeCategory> getRecipeCategories() {
		for (IRecipeCategory recipeCategory : this.checkIfEmptyRecipeCategories) {
			if (getRecipeWrappers(recipeCategory).isEmpty()) {
				this.emptyRecipeCategories.add(recipeCategory);
			}
		}
		this.checkIfEmptyRecipeCategories.clear();

		List<IRecipeCategory> recipeCategories = new ArrayList<>(this.recipeCategories);
		recipeCategories.removeAll(this.emptyRecipeCategories);
		return recipeCategories;
	}

	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids) {
		ErrorUtil.checkNotNull(recipeCategoryUids, "recipeCategoryUids");

		Set<String> uniqueUids = new HashSet<>();
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
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotEmpty(recipeCategoryUid, "recipeCategoryUid");
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
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Recipe check crashed", e);
				return null;
			}

			try {
				IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
				wrapperMap.put(recipe, recipeWrapper);
				return recipeWrapper;
			} catch (RuntimeException | LinkageError e) {
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
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");

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
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");

		List<IRecipeHandler<T>> recipeHandlers = new ArrayList<>();

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

		List<String> allRecipeCategoryUids = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<String> recipeCategoryUids = plugin.getRecipeCategoryUids(focus);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.get().warn("Recipe Category lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
			}
			allRecipeCategoryUids.addAll(recipeCategoryUids);
		}

		return getRecipeCategories(allRecipeCategoryUids);
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		focus = Focus.check(focus);

		FluidStack fluidStack = getFluidFromItemBlock(focus);
		if (fluidStack != null) {
			return getRecipeWrappers(recipeCategory, createFocus(focus.getMode(), fluidStack));
		}

		List<T> allRecipeWrappers = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory, focus);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.get().warn("Recipe Wrapper lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
			}
			allRecipeWrappers.addAll(recipeWrappers);
		}

		return allRecipeWrappers;
	}

	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		List<T> allRecipeWrappers = new ArrayList<>();
		for (IRecipeRegistryPlugin plugin : this.plugins) {
			long start_time = System.currentTimeMillis();
			List<T> recipeWrappers = plugin.getRecipeWrappers(recipeCategory);
			long timeElapsed = System.currentTimeMillis() - start_time;
			if (timeElapsed > 10) {
				Log.get().warn("Recipe Wrapper lookup is slow: {} ms. {}", timeElapsed, plugin.getClass());
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
		List<ItemStack> craftingItems = getCraftingItems(recipeCategory);

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
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		List<Object> objects = getRecipeCatalysts(recipeCategory);
		List<ItemStack> itemStacks = new ArrayList<>();
		for (Object object : objects) {
			if (object instanceof ItemStack) {
				itemStacks.add((ItemStack) object);
			}
		}
		return Collections.unmodifiableList(itemStacks);
	}

	@Override
	public List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		return recipeCatalysts.get(recipeCategory);
	}

	@Override
	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends Container> containerClass = container.getClass();
		IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return recipeTransferHandler;
		}

		return recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}

	@Override
	public <T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus<?> focus) {
		focus = Focus.check(focus);
		RecipeLayout recipeLayout = RecipeLayout.create(-1, recipeCategory, recipeWrapper, focus, 0, 0);
		Preconditions.checkNotNull(recipeLayout, "Recipe layout crashed during creation, see log.");
		return recipeLayout;
	}
}
