package mezz.jei;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class RecipeRegistry implements IRecipeRegistry {
	private final ImmutableMap<Class, IRecipeHandler> recipeHandlers;
	private final ImmutableClassToInstanceMap<IRecipeCategory> recipeCategoriesMap;
	private final ImmutableList<IRecipeCategory> recipeCategories;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;

	RecipeRegistry(@Nonnull ImmutableList<IRecipeCategory> recipeCategories, @Nonnull ImmutableList<IRecipeHandler> recipeHandlers, @Nonnull ImmutableList<Object> recipes) {
		this.recipeCategories = ImmutableSet.copyOf(recipeCategories).asList(); //remove duplicates
		this.recipeCategoriesMap = buildRecipeCategoriesMap(this.recipeCategories);
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);

		this.recipeInputMap = new RecipeMap(this);
		this.recipeOutputMap = new RecipeMap(this);
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
			if (recipeHandler == null)
				continue;

			Class recipeClass = recipeHandler.getRecipeClass();
			if (recipeClass == null)
				continue;

			if (mutableRecipeHandlers.containsKey(recipeClass))
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());

			mutableRecipeHandlers.put(recipeClass, recipeHandler);
		}
		return ImmutableMap.copyOf(mutableRecipeHandlers);
	}

	private void addRecipes(@Nullable ImmutableList<Object> recipes) {
		if (recipes == null)
			return;

		for (Object recipe : recipes) {
			if (recipe == null)
				continue;

			Class recipeClass = recipe.getClass();

			IRecipeHandler recipeHandler = getRecipeHandler(recipeClass);
			if (recipeHandler == null) {
				Log.debug("Can't handle recipe: " + recipe);
				continue;
			}
			Class<? extends IRecipeCategory> recipeCategoryClass = recipeHandler.getRecipeCategoryClass();
			IRecipeCategory recipeCategory = recipeCategoriesMap.getInstance(recipeCategoryClass);
			if (recipeCategory == null) {
				Log.error("No recipe category registered for recipeCategoryClass: " + recipeCategoryClass);
				continue;
			}

			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

			List inputs = recipeWrapper.getInputs();
			if (inputs != null) {
				List<ItemStack> inputStacks = StackUtil.toItemStackList(inputs);
				recipeInputMap.addRecipe(recipe, recipeCategory, inputStacks);
			}

			List outputs = recipeWrapper.getOutputs();
			if (outputs != null) {
				List<ItemStack> outputStacks = StackUtil.toItemStackList(outputs);
				recipeOutputMap.addRecipe(recipe, recipeCategory, outputStacks);
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
	public ImmutableList<IRecipeCategory> getRecipeCategoriesForInput(@Nullable ItemStack input) {
		if (input == null)
			return ImmutableList.of();
		return recipeInputMap.getRecipeCategories(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesForOutput(@Nullable ItemStack output) {
		if (output == null)
			return ImmutableList.of();
		return recipeOutputMap.getRecipeCategories(output);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getInputRecipes(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack input) {
		if (recipeCategory == null || input == null)
			return ImmutableList.of();
		return recipeInputMap.getRecipes(recipeCategory, input);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getOutputRecipes(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack output) {
		if (recipeCategory == null || output == null)
			return ImmutableList.of();
		return recipeOutputMap.getRecipes(recipeCategory, output);
	}

	public int getRecipeCategoryIndex(IRecipeCategory recipeCategory) {
		return recipeCategories.indexOf(recipeCategory);
	}
}
