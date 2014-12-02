package mezz.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry implements IRecipeRegistry {
	private final Map<Class, IRecipeHandler> recipeHandlers = new HashMap<Class, IRecipeHandler>();
	private final Map<Class<? extends IRecipeType>, IRecipeType> recipeTypesMap = new HashMap<Class<? extends IRecipeType>, IRecipeType>();
	private final List<IRecipeType> recipeTypes = new ArrayList<IRecipeType>();
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;

	public RecipeRegistry() {
		recipeInputMap = new RecipeMap(this);
		recipeOutputMap = new RecipeMap(this);
	}

	public void registerRecipeType(IRecipeType recipeType) {
		if (recipeTypesMap.containsKey(recipeType.getClass()))
			throw new IllegalArgumentException("This Recipe Type has already been registered: " + recipeType);

		recipeTypesMap.put(recipeType.getClass(), recipeType);
		recipeTypes.add(recipeType);
	}

	@Nullable
	public IRecipeType getRecipeType(Class<? extends IRecipeType> recipeTypeClass) {
		return recipeTypesMap.get(recipeTypeClass);
	}

	public final void registerRecipeHandlers(@Nullable IRecipeHandler... recipeHandlers) {
		if (recipeHandlers == null)
			return;

		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null)
				continue;

			Class recipeClass = recipeHandler.getRecipeClass();
			if (recipeClass == null)
				continue;

			if (this.recipeHandlers.containsKey(recipeClass))
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());

			this.recipeHandlers.put(recipeClass, recipeHandler);
		}
	}

	public void addRecipes(@Nullable Iterable recipes) {
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
