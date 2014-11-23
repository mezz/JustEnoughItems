package mezz.jei.recipes;

import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeRegistry;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry implements IRecipeRegistry {
	private final Map<Class, IRecipeHelper> recipeHelpers = new HashMap<Class, IRecipeHelper>();
	private final RecipeMap recipeInputMap = new RecipeMap();
	private final RecipeMap recipeOutputMap = new RecipeMap();

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
			IRecipeType recipeType = recipeHelper.getRecipeType();

			List<ItemStack> inputs = recipeHelper.getInputs(recipe);
			if (inputs != null) {
				inputs = StackUtil.getAllSubtypes(inputs);
				recipeInputMap.addRecipe(recipe, recipeType, inputs);
			}

			List<ItemStack> outputs = recipeHelper.getOutputs(recipe);
			if (outputs != null) {
				outputs = StackUtil.getAllSubtypes(outputs);
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
