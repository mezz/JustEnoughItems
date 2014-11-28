package mezz.jei;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
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

class RecipeRegistry implements IRecipeRegistry {
	private final Map<Class, IRecipeHandler> recipeHandlers = new HashMap<Class, IRecipeHandler>();
	private final Map<IRecipeTypeKey, IRecipeType> recipeTypesMap = new HashMap<IRecipeTypeKey, IRecipeType>();
	private final List<IRecipeType> recipeTypes = new ArrayList<IRecipeType>();
	private final RecipeMap recipeInputMap = new RecipeMap();
	private final RecipeMap recipeOutputMap = new RecipeMap();

	@Override
	public void registerRecipeType(IRecipeTypeKey recipeTypeKey, IRecipeType recipeType) {
		if (recipeTypesMap.containsKey(recipeTypeKey))
			throw new IllegalArgumentException("A Recipe Type has already been registered for this recipeTypeKey: " + recipeTypeKey);

		recipeTypesMap.put(recipeTypeKey, recipeType);
		recipeTypes.add(recipeType);
	}

	@Nullable
	@Override
	public IRecipeType getRecipeType(IRecipeTypeKey recipeTypeKey) {
		return recipeTypesMap.get(recipeTypeKey);
	}

	@Override
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

	@Override
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
			IRecipeTypeKey recipeTypeKey = recipeHandler.getRecipeTypeKey();
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

			List<ItemStack> outputs = recipeWrapper.getOutputs();
			if (outputs != null) {
				outputs = StackUtil.toItemStackList(outputs);
				recipeOutputMap.addRecipe(recipe, recipeType, outputs);
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
	public List<IRecipeType> getRecipeTypesForInput(@Nullable ItemStack input) {
		if (input == null)
			return Collections.emptyList();
		return recipeInputMap.getRecipeTypes(input);
	}

	@Nonnull
	@Override
	public List<IRecipeType> getRecipeTypesForOutput(@Nullable ItemStack output) {
		if (output == null)
			return Collections.emptyList();
		return recipeOutputMap.getRecipeTypes(output);
	}

	@Nonnull
	@Override
	public List<Object> getInputRecipes(@Nullable IRecipeType recipeType, @Nullable ItemStack input) {
		if (recipeType == null || input == null)
			return Collections.emptyList();
		return recipeInputMap.getRecipes(recipeType, input);
	}

	@Nonnull
	@Override
	public List<Object> getOutputRecipes(@Nullable IRecipeType recipeType, @Nullable ItemStack output) {
		if (recipeType == null || output == null)
			return Collections.emptyList();
		return recipeOutputMap.getRecipes(recipeType, output);
	}

	@Override
	public int getRecipeTypeIndex(IRecipeType recipeType) {
		return recipeTypes.indexOf(recipeType);
	}
}
