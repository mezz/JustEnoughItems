package mezz.jei.recipes;

import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeRegistry;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry implements IRecipeRegistry {
	private final Map<Class, IRecipeHelper> recipeHelpers = new HashMap<Class, IRecipeHelper>();
	private final RecipeMap recipeInputMap = new RecipeMap();
	private final RecipeMap recipeOutputMap = new RecipeMap();

	@Override
	public void registerRecipeHelper(IRecipeHelper recipeHelper) {
		Class recipeClass = recipeHelper.getRecipeClass();

		if (recipeHelpers.containsKey(recipeClass))
			throw new IllegalArgumentException("A Recipe Helper has already been registered for this recipe class: " + recipeClass.getName());

		recipeHelpers.put(recipeClass, recipeHelper);
	}

	@Override
	public void addRecipes(Iterable<Object> recipes) {
		for (Object recipe : recipes) {
			Class recipeClass = recipe.getClass();

			IRecipeHelper recipeHelper = getRecipeHelper(recipeClass);
			if (recipeHelper == null) {
				Log.debug("Can't handle recipe: " + recipe.toString());
				continue;
			}
			IRecipeType recipeType = recipeHelper.getRecipeType();

			List<ItemStack> inputs = recipeHelper.getInputs(recipe);
			inputs = StackUtil.removeDuplicateItemStacks(inputs);

			List<ItemStack> outputs = recipeHelper.getOutputs(recipe);
			outputs = StackUtil.removeDuplicateItemStacks(outputs);

			recipeInputMap.addRecipe(recipe, recipeType, inputs);
			recipeOutputMap.addRecipe(recipe, recipeType, outputs);
		}
	}

	@Override
	public IRecipeHelper getRecipeHelper(Class recipeClass) {
		IRecipeHelper recipeHelper = recipeHelpers.get(recipeClass);
		if (recipeHelper != null)
			return recipeHelper;

		Class superClass = recipeClass.getSuperclass();
		if (superClass != null && superClass != Object.class)
			return getRecipeHelper(superClass);

		return null;
	}

	@Override
	public List<IRecipeType> getRecipeTypesForInput(ItemStack input) {
		return recipeInputMap.getRecipeTypes(input);
	}

	@Override
	public List<IRecipeType> getRecipeTypesForOutput(ItemStack output) {
		return recipeOutputMap.getRecipeTypes(output);
	}

	@Override
	public List<Object> getInputRecipes(IRecipeType recipeType, ItemStack input) {
		return recipeInputMap.getRecipes(recipeType, input);
	}

	@Override
	public List<Object> getOutputRecipes(IRecipeType recipeType, ItemStack output) {
		return recipeOutputMap.getRecipes(recipeType, output);
	}
}
