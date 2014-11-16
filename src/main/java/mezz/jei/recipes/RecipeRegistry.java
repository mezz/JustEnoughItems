package mezz.jei.recipes;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeRegistry;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry implements IRecipeRegistry {
	private static final Map<Class, IRecipeHelper> recipeHelpers = new HashMap<Class, IRecipeHelper>();

	/**
	 * List of recipes, keyed by recipe class and inputs/outputs for the recipe.
	 * Recipe Type:
	 *   String Key of ItemStack input/output:
	 *     List of recipes with input/output
	 */
	private final Map<IRecipeType, Map<String, List<Object>>> recipeInputMaps = new HashMap<IRecipeType, Map<String, List<Object>>>();
	private final Map<IRecipeType, Map<String, List<Object>>> recipeOutputMaps = new HashMap<IRecipeType, Map<String, List<Object>>>();

	/**
	 * List of Recipe Type for each ItemStack input/output.
	 * For fast access in getRecipeTypesForInput and getRecipeTypesForOutput.
	 */
	private final Map<String, List<IRecipeType>> recipeTypeInputMap = new HashMap<String, List<IRecipeType>>();
	private final Map<String, List<IRecipeType>> recipeTypeOutputMap = new HashMap<String, List<IRecipeType>>();

	private Map<String, List<Object>> getRecipeMap(Map<IRecipeType, Map<String, List<Object>>> recipeMaps, IRecipeType recipeType) {
		Map<String, List<Object>> recipeMap = recipeMaps.get(recipeType);
		if (recipeMap == null) {
			recipeMap = new HashMap<String, List<Object>>();
			recipeMaps.put(recipeType, recipeMap);
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

	private List<IRecipeType> getRecipeTypeList(Map<String, List<IRecipeType>> recipeTypeMap, String stackKey) {
		List<IRecipeType> recipeTypeList = recipeTypeMap.get(stackKey);
		if (recipeTypeList == null) {
			recipeTypeList = new ArrayList<IRecipeType>();
			recipeTypeMap.put(stackKey, recipeTypeList);
		}
		return recipeTypeList;
	}

	private String asKey(ItemStack itemstack) {
		return itemstack.getUnlocalizedName() + ":" + GameData.getItemRegistry().getId(itemstack.getItem());
	}

	@Override
	public void addRecipes(Iterable<Object> recipes) {
		for (Object recipe : recipes) {
			Class recipeClass = recipe.getClass();

			IRecipeHelper recipeHelper = getRecipeHelper(recipeClass);
			if (recipeHelper == null) {
				Log.warning("Can't handle recipe: " + recipe.toString());
				continue;
			}
			IRecipeType recipeType = recipeHelper.getRecipeType();

			Map<String, List<Object>> recipeInputs = getRecipeMap(recipeInputMaps, recipeType);
			Map<String, List<Object>> recipeOutputs = getRecipeMap(recipeOutputMaps, recipeType);

			List<ItemStack> inputs = StackUtil.removeDuplicateItemStacks(getInputs(recipe));
			List<ItemStack> outputs = StackUtil.removeDuplicateItemStacks(getOutputs(recipe));

			for (ItemStack input : inputs) {
				String inputKey = asKey(input);
				getRecipeList(recipeInputs, inputKey).add(recipe);

				List<IRecipeType> recipeTypes = getRecipeTypeList(recipeTypeInputMap, inputKey);
				if (!recipeTypes.contains(recipeType))
					recipeTypes.add(recipeType);
			}

			for (ItemStack output : outputs) {
				String outputKey = asKey(output);
				getRecipeList(recipeOutputs, outputKey).add(recipe);

				List<IRecipeType> recipeTypes = getRecipeTypeList(recipeTypeOutputMap, outputKey);
				if (!recipeTypes.contains(recipeType))
					recipeTypes.add(recipeType);
			}
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
	public List<IRecipeType> getRecipeTypesForInput(ItemStack input) {
		String key = asKey(input);
		return recipeTypeInputMap.get(key);
	}

	@Override
	public List<IRecipeType> getRecipeTypesForOutput(ItemStack output) {
		String key = asKey(output);
		return recipeTypeOutputMap.get(key);
	}

	@Override
	public List<Object> getInputRecipes(IRecipeType recipeType, ItemStack input) {
		Map<String, List<Object>> recipeMap = getRecipeMap(recipeInputMaps, recipeType);
		String key = asKey(input);
		return getRecipeList(recipeMap, key);
	}

	@Override
	public List<Object> getOutputRecipes(IRecipeType recipeType, ItemStack output) {
		Map<String, List<Object>> recipeMap = getRecipeMap(recipeOutputMaps, recipeType);
		String key = asKey(output);
		return getRecipeList(recipeMap, key);
	}
}
