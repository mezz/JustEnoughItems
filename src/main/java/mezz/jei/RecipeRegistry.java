package mezz.jei;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
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
	private final ImmutableClassToInstanceMap<IRecipeType> recipeTypesMap;
	private final ImmutableList<IRecipeType> recipeTypes;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;

	public RecipeRegistry(@Nonnull ImmutableList<IRecipeType> recipeTypes, @Nonnull ImmutableList<IRecipeHandler> recipeHandlers, @Nonnull ImmutableList<Object> recipes) {
		this.recipeTypes = ImmutableSet.copyOf(recipeTypes).asList(); //remove duplicates
		this.recipeTypesMap = buildRecipeTypesMap(this.recipeTypes);
		this.recipeHandlers = buildRecipeHandlersMap(recipeHandlers);

		this.recipeInputMap = new RecipeMap(this);
		this.recipeOutputMap = new RecipeMap(this);
		addRecipes(recipes);
	}

	private static ImmutableClassToInstanceMap<IRecipeType> buildRecipeTypesMap(@Nonnull ImmutableList<IRecipeType> recipeTypes) {
		MutableClassToInstanceMap<IRecipeType> mutableRecipeTypesMap = MutableClassToInstanceMap.create();
		for (IRecipeType recipeType : recipeTypes) {
			mutableRecipeTypesMap.put(recipeType.getClass(), recipeType);
		}
		return ImmutableClassToInstanceMap.copyOf(mutableRecipeTypesMap);
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
			Class<? extends IRecipeType> recipeTypeKey = recipeHandler.getRecipeTypeClass();
			IRecipeType recipeType = getRecipeType(recipeTypeKey);
			if (recipeType == null) {
				Log.error("No recipe type registered for key: " + recipeTypeKey);
				continue;
			}

			IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

			List inputs = recipeWrapper.getInputs();
			if (inputs != null) {
				List<ItemStack> inputStacks = StackUtil.toItemStackList(inputs);
				recipeInputMap.addRecipe(recipe, recipeType, inputStacks);
			}

			List outputs = recipeWrapper.getOutputs();
			if (outputs != null) {
				List<ItemStack> outputStacks = StackUtil.toItemStackList(outputs);
				recipeOutputMap.addRecipe(recipe, recipeType, outputStacks);
			}
		}
	}

	@Nullable
	public IRecipeType getRecipeType(Class<? extends IRecipeType> recipeTypeClass) {
		return recipeTypesMap.getInstance(recipeTypeClass);
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
	public ImmutableList<IRecipeType> getRecipeTypesForInput(@Nullable ItemStack input) {
		if (input == null)
			return ImmutableList.of();
		return recipeInputMap.getRecipeTypes(input);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeType> getRecipeTypesForOutput(@Nullable ItemStack output) {
		if (output == null)
			return ImmutableList.of();
		return recipeOutputMap.getRecipeTypes(output);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getInputRecipes(@Nullable IRecipeType recipeType, @Nullable ItemStack input) {
		if (recipeType == null || input == null)
			return ImmutableList.of();
		return recipeInputMap.getRecipes(recipeType, input);
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getOutputRecipes(@Nullable IRecipeType recipeType, @Nullable ItemStack output) {
		if (recipeType == null || output == null)
			return ImmutableList.of();
		return recipeOutputMap.getRecipes(recipeType, output);
	}

	public int getRecipeTypeIndex(IRecipeType recipeType) {
		return recipeTypes.indexOf(recipeType);
	}
}
