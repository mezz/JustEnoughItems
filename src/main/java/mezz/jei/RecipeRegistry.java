package mezz.jei;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeCategoryComparator;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackUtil;

public class RecipeRegistry implements IRecipeRegistry {
	private final ImmutableMap<Class, IRecipeHandler> recipeHandlers;
	private final ImmutableClassToInstanceMap<IRecipeCategory> recipeCategoriesMap;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses = new HashSet<>();

	RecipeRegistry(@Nonnull ImmutableList<IRecipeCategory> recipeCategories, @Nonnull ImmutableList<IRecipeHandler> recipeHandlers, @Nonnull ImmutableList<Object> recipes) {
		recipeCategories = ImmutableSet.copyOf(recipeCategories).asList(); //remove duplicates
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator);

		addRecipes(recipes);
	}

	private static ImmutableClassToInstanceMap<IRecipeCategory> buildRecipeCategoriesMap(@Nonnull ImmutableList<IRecipeCategory> recipeCategories) {
		MutableClassToInstanceMap<IRecipeCategory> mutableRecipeCategoriesMap = MutableClassToInstanceMap.create();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mutableRecipeCategoriesMap.put(recipeCategory.getClass(), recipeCategory);
		}
		return ImmutableClassToInstanceMap.copyOf(mutableRecipeCategoriesMap);
	}

	private static ImmutableMap<Class, IRecipeHandler> buildRecipeHandlersMap(@Nonnull List<IRecipeHandler> recipeHandlers) {
		HashMap<Class, IRecipeHandler> mutableRecipeHandlers = Maps.newHashMap();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass = recipeHandler.getRecipeClass();
			if (recipeClass == null) {
				continue;
			}

			if (mutableRecipeHandlers.containsKey(recipeClass)) {
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}

			mutableRecipeHandlers.put(recipeClass, recipeHandler);
		}
		return ImmutableMap.copyOf(mutableRecipeHandlers);
	}

	private void addRecipes(@Nullable ImmutableList<Object> recipes) {
		if (recipes == null) {
			return;
		}

		for (Object recipe : recipes) {
			if (recipe == null) {
				continue;
			}

			Class recipeClass = recipe.getClass();

			IRecipeHandler recipeHandler = getRecipeHandler(recipeClass);
			if (recipeHandler == null) {
				if (!unhandledRecipeClasses.contains(recipeClass)) {
					unhandledRecipeClasses.add(recipeClass);
					Log.debug("Can't handle recipe: {}", recipeClass);
				}
				continue;
			}
			Class recipeCategoryClass = recipeHandler.getRecipeCategoryClass();
			IRecipeCategory recipeCategory = recipeCategoriesMap.getInstance(recipeCategoryClass);
			if (recipeCategory == null) {
				Log.error("No recipe category registered for recipeCategoryClass: {}", recipeCategoryClass);
				continue;
			}

			//noinspection unchecked
			if (!recipeHandler.isRecipeValid(recipe)) {
				continue;
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
		}
	}

	@Nullable
	@Override
	public IRecipeHandler getRecipeHandler(@Nonnull Class recipeClass) {
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
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipeCategories(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable Fluid input) {
		if (input == null) {
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipeCategories(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable ItemStack output) {
		if (output == null) {
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipeCategories(output);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable Fluid output) {
		if (output == null) {
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipeCategories(output);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack input) {
		if (recipeCategory == null || input == null) {
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipes(recipeCategory, input);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid input) {
		if (recipeCategory == null || input == null) {
			return ImmutableList.of();
		}
		return recipeInputMap.getRecipes(recipeCategory, input);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack output) {
		if (recipeCategory == null || output == null) {
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipes(recipeCategory, output);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid output) {
		if (recipeCategory == null || output == null) {
			return ImmutableList.of();
		}
		return recipeOutputMap.getRecipes(recipeCategory, output);
	}
}
