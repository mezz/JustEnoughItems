package mezz.jei;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeCategoryComparator;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackUtil;

public class RecipeRegistry implements IRecipeRegistry {
	private final ImmutableMap<Class, IRecipeHandler> recipeHandlers;
	private final ImmutableTable<Class, String, IRecipeTransferHelper> recipeTransferHelpers;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final ListMultimap<IRecipeCategory, Object> recipesForCategories;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses;

	public RecipeRegistry(@Nonnull List<IRecipeCategory> recipeCategories, @Nonnull List<IRecipeHandler> recipeHandlers, @Nonnull List<IRecipeTransferHelper> recipeTransferHelpers, @Nonnull List<Object> recipes) {
		recipeCategories = ImmutableSet.copyOf(recipeCategories).asList(); //remove duplicates
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHelpers = buildRecipeTransferHelperTable(recipeTransferHelpers);
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator);

		this.unhandledRecipeClasses = new HashSet<>();

		this.recipesForCategories = ArrayListMultimap.create();
		addRecipes(recipes);
	}

	private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(@Nonnull List<IRecipeCategory> recipeCategories) {
		Map<String, IRecipeCategory> mutableRecipeCategoriesMap = new HashMap<>();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mutableRecipeCategoriesMap.put(recipeCategory.getUid(), recipeCategory);
		}
		return ImmutableMap.copyOf(mutableRecipeCategoriesMap);
	}

	private static ImmutableMap<Class, IRecipeHandler> buildRecipeHandlersMap(@Nonnull List<IRecipeHandler> recipeHandlers) {
		HashMap<Class, IRecipeHandler> mutableRecipeHandlers = Maps.newHashMap();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass = recipeHandler.getRecipeClass();

			if (mutableRecipeHandlers.containsKey(recipeClass)) {
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}

			mutableRecipeHandlers.put(recipeClass, recipeHandler);
		}
		return ImmutableMap.copyOf(mutableRecipeHandlers);
	}

	private static ImmutableTable<Class, String, IRecipeTransferHelper> buildRecipeTransferHelperTable(@Nonnull List<IRecipeTransferHelper> recipeTransferHelpers) {
		ImmutableTable.Builder<Class, String, IRecipeTransferHelper> builder = ImmutableTable.builder();
		for (IRecipeTransferHelper recipeTransferHelper : recipeTransferHelpers) {
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

		try {
			addRecipeUnchecked(recipe);
		} catch (RuntimeException e) {
			Log.error("Failed to add recipe: {}", recipe, e);
		}
	}

	private void addRecipeUnchecked(@Nonnull Object recipe) throws RuntimeException {
		Class recipeClass = recipe.getClass();
		IRecipeHandler recipeHandler = getRecipeHandler(recipeClass);
		if (recipeHandler == null) {
			if (!unhandledRecipeClasses.contains(recipeClass)) {
				unhandledRecipeClasses.add(recipeClass);
				Log.debug("Can't handle recipe: {}", recipeClass);
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

		//noinspection unchecked
		IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

		List inputs = recipeWrapper.getInputs();
		List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
		if (inputs != null || fluidInputs != null) {
			List<ItemStack> inputStacks = StackUtil.toItemStackList(inputs);
			if (fluidInputs == null) {
				fluidInputs = Collections.emptyList();
			}
			recipeInputMap.addRecipe(recipe, recipeCategory, inputStacks, fluidInputs);
		}

		List outputs = recipeWrapper.getOutputs();
		List<FluidStack> fluidOutputs = recipeWrapper.getFluidOutputs();
		if (outputs != null || fluidOutputs != null) {
			List<ItemStack> outputStacks = StackUtil.toItemStackList(outputs);
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
			if (getRecipes(recipeCategory).size() > 0) {
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
	@Override
	public IRecipeTransferHelper getRecipeTransferHelper(@Nullable Container container, @Nullable IRecipeCategory recipeCategory) {
		if (container == null) {
			Log.error("Null container", new NullPointerException());
			return null;
		} else if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return null;
		}
		return recipeTransferHelpers.get(container.getClass(), recipeCategory.getUid());
	}
}
