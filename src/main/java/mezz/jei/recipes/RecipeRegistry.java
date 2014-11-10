package mezz.jei.recipes;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry implements IRecipeRegistry {
	private static final HashMap<Class, IRecipeHelper> recipeHelpers = new HashMap<Class, IRecipeHelper>();

	/**
	 * List of recipes, keyed by recipe class and inputs/outputs for the recipe.
	 * Recipe Class:
	 *   String of ItemStack input/output:
	 *     List of recipes with input/output
	 */
	private final Map<Class, Map<String, List<Object>>> recipeInputMaps = new HashMap<Class, Map<String, List<Object>>>();
	private final Map<Class, Map<String, List<Object>>> recipeOutputMaps = new HashMap<Class, Map<String, List<Object>>>();

	/**
	 * List of Recipe Classes for each ItemStack input/output.
	 * For fast access in getRecipeClassesForOutput and getRecipeClassesForInput.
	 */
	private final Map<String, List<Class>> recipeClassInputMap = new HashMap<String, List<Class>>();
	private final Map<String, List<Class>> recipeClassOutputMap = new HashMap<String, List<Class>>();

	private Map<String, List<Object>> getRecipeMap(Map<Class, Map<String, List<Object>>> recipeMaps, Class recipeClass) {
		Map<String, List<Object>> recipeMap = recipeMaps.get(recipeClass);
		if (recipeMap == null) {
			recipeMap = new HashMap<String, List<Object>>();
			recipeMaps.put(recipeClass, recipeMap);
		}
		return recipeMap;
	}

	private List<Object> getRecipeList(Map<String, List<Object>> recipeMap, String stackKey) {
		List<Object> recipeInputList = recipeMap.get(stackKey);
		if (recipeInputList == null) {
			recipeInputList = new ArrayList<Object>();
			recipeMap.put(stackKey, recipeInputList);
		}
		return recipeInputList;
	}

	private List<Class> getRecipeClassList(Map<String, List<Class>> recipeClassMap, String stackKey) {
		List<Class> recipeClassList = recipeClassMap.get(stackKey);
		if (recipeClassList == null) {
			recipeClassList = new ArrayList<Class>();
			recipeClassMap.put(stackKey, recipeClassList);
		}
		return recipeClassList;
	}

	private String asKey(ItemStack itemstack) {
		return itemstack.getUnlocalizedName() + ":" + GameData.getItemRegistry().getId(itemstack.getItem());
	}

	@Override
	public void addRecipes(Iterable<Object> recipes) {
		for (Object recipe : recipes) {
			Class recipeClass = recipe.getClass();

			if (!hasRecipeHelper(recipeClass)) {
				Log.warning("Can't handle recipe: " + recipe.toString());
				continue;
			}

			Map<String, List<Object>> recipeInputs = getRecipeMap(recipeInputMaps, recipeClass);
			Map<String, List<Object>> recipeOutputs = getRecipeMap(recipeOutputMaps, recipeClass);

			List<ItemStack> inputs = StackUtil.removeDuplicateItemStacks(getInputs(recipe));
			List<ItemStack> outputs = StackUtil.removeDuplicateItemStacks(getOutputs(recipe));

			for (ItemStack input : inputs) {
				String inputKey = asKey(input);
				getRecipeList(recipeInputs, inputKey).add(recipe);

				List<Class> recipeClasses = getRecipeClassList(recipeClassInputMap, inputKey);
				if (!recipeClasses.contains(recipeClass))
					recipeClasses.add(recipeClass);
			}

			for (ItemStack output : outputs) {
				String outputKey = asKey(output);
				getRecipeList(recipeOutputs, outputKey).add(recipe);

				List<Class> recipeClasses = getRecipeClassList(recipeClassOutputMap, outputKey);
				if (!recipeClasses.contains(recipeClass))
					recipeClasses.add(recipeClass);
			}
		}
	}

	@Override
	public IRecipeHelper getRecipeHelper(Class recipeClass) {
		return recipeHelpers.get(recipeClass);
	}

	@Override
	public void registerRecipeHelper(IRecipeHelper recipeHelper) {
		Class recipeClass = recipeHelper.getRecipeClass();

		if (recipeHelpers.containsKey(recipeClass))
			throw new IllegalArgumentException("A Recipe Helper has already been registered for this recipe type: " + recipeClass.getName());

		recipeHelpers.put(recipeClass, recipeHelper);
	}

	public List<ItemStack> getInputs(Object recipe) {
		Class recipeClass = recipe.getClass();
		IRecipeHelper recipeHelper = recipeHelpers.get(recipeClass);
		if (recipeHelper == null)
			return null;
		return recipeHelper.getInputs(recipe);
	}

	public List<ItemStack> getOutputs(Object recipe) {
		Class recipeClass = recipe.getClass();
		IRecipeHelper recipeHelper = recipeHelpers.get(recipeClass);
		if (recipeHelper == null)
			return null;
		return recipeHelper.getOutputs(recipe);
	}

	@Override
	public boolean hasRecipeHelper(Class recipeClass) {
		return recipeHelpers.containsKey(recipeClass);
	}

	@Override
	public List<Class> getInputRecipeClasses(ItemStack input) {
		String key = asKey(input);
		return recipeClassInputMap.get(key);
	}

	@Override
	public List<Class> getOutputRecipeClasses(ItemStack output) {
		String key = asKey(output);
		return recipeClassOutputMap.get(key);
	}

	@Override
	public List<Object> getInputRecipes(Class recipeClass, ItemStack input) {
		Map<String, List<Object>> recipeMap = getRecipeMap(recipeInputMaps, recipeClass);
		String key = asKey(input);
		return getRecipeList(recipeMap, key);
	}

	@Override
	public List<Object> getOutputRecipes(Class recipeClass, ItemStack output) {
		Map<String, List<Object>> recipeMap = getRecipeMap(recipeOutputMaps, recipeClass);
		String key = asKey(output);
		return getRecipeList(recipeMap, key);
	}
}
