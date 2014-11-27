package mezz.jei;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeHelper;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RecipeRegistry implements IRecipeRegistry {
	private final Map<Class, IRecipeHelper> recipeHelpers = new HashMap<Class, IRecipeHelper>();
	private final Map<IRecipeTypeKey, IRecipeType> recipeTypes = new HashMap<IRecipeTypeKey, IRecipeType>();
	private final RecipeMap recipeInputMap = new RecipeMap();
	private final RecipeMap recipeOutputMap = new RecipeMap();

	@Override
	public void registerRecipeType(IRecipeTypeKey recipeTypeKey, IRecipeType recipeType) {
		if (recipeTypes.containsKey(recipeTypeKey))
			throw new IllegalArgumentException("A Recipe Type has already been registered for this recipeTypeKey: " + recipeTypeKey);

		recipeTypes.put(recipeTypeKey, recipeType);
	}

	@Nullable
	@Override
	public IRecipeType getRecipeType(IRecipeTypeKey recipeTypeKey) {
		return recipeTypes.get(recipeTypeKey);
	}

	@Override
	public final void registerRecipeHelpers(@Nullable IRecipeHelper... recipeHelpers) {
		if (recipeHelpers == null)
			return;

		for (IRecipeHelper recipeHelper : recipeHelpers) {
			if (recipeHelper == null)
				continue;

			Class recipeClass = recipeHelper.getRecipeClass();
			if (recipeClass == null)
				continue;

			if (this.recipeHelpers.containsKey(recipeClass))
				throw new IllegalArgumentException("A Recipe Helper has already been registered for this recipe class: " + recipeClass.getName());

			this.recipeHelpers.put(recipeClass, recipeHelper);
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

			IRecipeHelper recipeHelper = getRecipeHelper(recipeClass);
			if (recipeHelper == null) {
				Log.debug("Can't handle recipe: " + recipe);
				continue;
			}
			IRecipeTypeKey recipeTypeKey = recipeHelper.getRecipeTypeKey();
			IRecipeType recipeType = getRecipeType(recipeTypeKey);
			if (recipeType == null) {
				Log.error("No recipe type registered for key: " + recipeTypeKey);
				continue;
			}

			IRecipeWrapper recipeWrapper = recipeHelper.getRecipeWrapper(recipe);

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
	public IRecipeHelper getRecipeHelper(@Nonnull Class recipeClass) {
		IRecipeHelper recipeHelper;
		while ((recipeHelper = recipeHelpers.get(recipeClass)) == null && (recipeClass != Object.class)) {
			recipeClass = recipeClass.getSuperclass();
		}

		return recipeHelper;
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
}
