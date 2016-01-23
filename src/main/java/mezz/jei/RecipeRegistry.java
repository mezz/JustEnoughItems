package mezz.jei;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Config;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.util.ItemUidException;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeCategoryComparator;
import mezz.jei.util.RecipeMap;

public class RecipeRegistry implements IRecipeRegistry {
	private final ImmutableMap<Class, IRecipeHandler> recipeHandlers;
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final ListMultimap<IRecipeCategory, Object> recipesForCategories;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses;

	public RecipeRegistry(@Nonnull List<IRecipeCategory> recipeCategories, @Nonnull List<IRecipeHandler> recipeHandlers, @Nonnull List<IRecipeTransferHandler> recipeTransferHandlers, @Nonnull List<Object> recipes, @Nonnull Map<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreas) {
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = buildRecipeTransferHandlerTable(recipeTransferHandlers);
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);
		this.recipeClickableAreas = ImmutableMap.copyOf(recipeClickableAreas);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator);

		this.unhandledRecipeClasses = new HashSet<>();

		this.recipesForCategories = ArrayListMultimap.create();
		addRecipes(recipes);
	}

	private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(@Nonnull List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private static ImmutableMap<Class, IRecipeHandler> buildRecipeHandlersMap(@Nonnull List<IRecipeHandler> recipeHandlers) {
		ImmutableMap.Builder<Class, IRecipeHandler> mapBuilder = ImmutableMap.builder();
		Set<Class> recipeHandlerClasses = new HashSet<>();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass = recipeHandler.getRecipeClass();

			if (recipeHandlerClasses.contains(recipeClass)) {
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}

			recipeHandlerClasses.add(recipeClass);
			mapBuilder.put(recipeClass, recipeHandler);
		}
		return mapBuilder.build();
	}

	private static ImmutableTable<Class, String, IRecipeTransferHandler> buildRecipeTransferHandlerTable(@Nonnull List<IRecipeTransferHandler> recipeTransferHandlers) {
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
	public void addRecipe(@Nullable Object recipe) {
		if (recipe == null) {
			Log.error("Null recipe", new NullPointerException());
			return;
		}

		Class recipeClass = recipe.getClass();
		IRecipeHandler recipeHandler = getRecipeHandler(recipeClass);
		if (recipeHandler == null) {
			if (!unhandledRecipeClasses.contains(recipeClass)) {
				unhandledRecipeClasses.add(recipeClass);
				if (Config.isDebugModeEnabled()) {
					Log.debug("Can't handle recipe: {}", recipeClass);
				}
			}
			return;
		}

		String recipeCategoryUid = recipeHandler.getRecipeCategoryUid();
		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		//noinspection unchecked
		if (!recipeHandler.isRecipeValid(recipe)) {
			return;
		}

		try {
			addRecipeUnchecked(recipe, recipeCategory, recipeHandler);
		} catch (RuntimeException e) {
			String recipeInfo = getInfoFromBrokenRecipe(recipe, recipeHandler);
			if (e instanceof ItemUidException) {
				Log.error("Found broken recipe: {}\n{}\n", e.getMessage(), recipeInfo);
			} else {
				Log.error("Found broken recipe: {}\n", recipeInfo, e);
			}
		}
	}

	@Nonnull
	private String getInfoFromBrokenRecipe(@Nonnull Object recipe, @Nonnull IRecipeHandler recipeHandler) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		try {
			recipeInfoBuilder.append(recipe);
		} catch (RuntimeException e) {
			Log.error("Failed recipe.toString", e);
			recipeInfoBuilder.append(recipe.getClass());
		}

		IRecipeWrapper recipeWrapper;

		try {
			//noinspection unchecked
			recipeWrapper = recipeHandler.getRecipeWrapper(recipe);
		} catch (RuntimeException ignored) {
			recipeInfoBuilder.append("\nFailed to create recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append("\nOutput ItemStacks: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getOutputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nOutput Fluids: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getFluidOutputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nInput ItemStacks: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getInputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		recipeInfoBuilder.append("\nInput Fluids: ");
		try {
			recipeInfoBuilder.append(recipeWrapper.getFluidInputs());
		} catch (RuntimeException e) {
			recipeInfoBuilder.append(e.getMessage());
		}

		return recipeInfoBuilder.toString();
	}

	private void addRecipeUnchecked(@Nonnull Object recipe, IRecipeCategory recipeCategory, IRecipeHandler recipeHandler) {
		//noinspection unchecked
		IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

		List inputs = recipeWrapper.getInputs();
		List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
		if (inputs != null || fluidInputs != null) {
			List<ItemStack> inputStacks = Internal.getStackHelper().toItemStackList(inputs);
			if (fluidInputs == null) {
				fluidInputs = Collections.emptyList();
			}
			recipeInputMap.addRecipe(recipe, recipeCategory, inputStacks, fluidInputs);
		}

		List outputs = recipeWrapper.getOutputs();
		List<FluidStack> fluidOutputs = recipeWrapper.getFluidOutputs();
		if (outputs != null || fluidOutputs != null) {
			List<ItemStack> outputStacks = Internal.getStackHelper().toItemStackList(outputs);
			if (fluidOutputs == null) {
				fluidOutputs = Collections.emptyList();
			}
			recipeOutputMap.addRecipe(recipe, recipeCategory, outputStacks, fluidOutputs);
		}

		recipesForCategories.put(recipeCategory, recipe);
	}

	@Nonnull
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

	@Nonnull
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
	public IRecipeHandler getRecipeHandler(@Nullable Class recipeClass) {
		if (recipeClass == null) {
			Log.error("Null recipeClass", new NullPointerException());
			return null;
		}

		IRecipeHandler recipeHandler;
		while ((recipeHandler = recipeHandlers.get(recipeClass)) == null && (recipeClass != Object.class)) {
			recipeClass = recipeClass.getSuperclass();
		}

		return recipeHandler;
	}

	@Nullable
	public RecipeClickableArea getRecipeClickableArea(@Nonnull GuiContainer gui) {
		return recipeClickableAreas.get(gui.getClass());
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable ItemStack input) {
		if (input == null) {
			Log.error("Null ItemStack input", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipeCategories(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable Fluid input) {
		if (input == null) {
			Log.error("Null Fluid input", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipeCategories(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable ItemStack output) {
		if (output == null) {
			Log.error("Null ItemStack output", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipeCategories(output);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable Fluid output) {
		if (output == null) {
			Log.error("Null Fluid output", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipeCategories(output);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack input) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		} else if (input == null) {
			Log.error("Null ItemStack input", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipes(recipeCategory, input);
	}

	@Nonnull
	@Override
	public List<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid input) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		} else if (input == null) {
			Log.error("Null Fluid input", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipes(recipeCategory, input);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack output) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		} else if (output == null) {
			Log.error("Null ItemStack output", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipes(recipeCategory, output);
	}

	@Nonnull
	@Override
	public List<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid output) {
		if (recipeCategory == null) {
			return ImmutableList.of();
		} else if (output == null) {
			Log.error("Null Fluid output", new NullPointerException());
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipes(recipeCategory, output);
	}

	@Nonnull
	@Override
	public List<Object> getRecipes(@Nullable IRecipeCategory recipeCategory) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		}
		return Collections.unmodifiableList(recipesForCategories.get(recipeCategory));
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
