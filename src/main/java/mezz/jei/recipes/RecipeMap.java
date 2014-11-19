package mezz.jei.recipes;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.api.recipes.IRecipeType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A RecipeMap efficiently links Recipes, IRecipeTypes, and ItemStacks.
 */
public class RecipeMap {

	private final Map<IRecipeType, RecipesForStack> recipeMap = new HashMap<IRecipeType, RecipesForStack>();
	private final Map<String, List<IRecipeType>> typeMap = new HashMap<String, List<IRecipeType>>();

	private static String asKey(ItemStack itemstack) {
		return itemstack.getUnlocalizedName() + ":" + GameData.getItemRegistry().getId(itemstack.getItem());
	}

	private RecipesForStack getRecipesForType(IRecipeType recipeType) {
		RecipesForStack recipesForStack = recipeMap.get(recipeType);
		if (recipesForStack == null) {
			recipesForStack = new RecipesForStack();
			recipeMap.put(recipeType, recipesForStack);
		}
		return recipesForStack;
	}

	public List<IRecipeType> getRecipeTypes(ItemStack itemStack) {
		String stackKey = asKey(itemStack);
		List<IRecipeType> recipeTypes = typeMap.get(stackKey);
		if (recipeTypes == null) {
			recipeTypes = new ArrayList<IRecipeType>();
			typeMap.put(stackKey, recipeTypes);
		}
		return recipeTypes;
	}

	public List<Object> getRecipes(IRecipeType recipeType, ItemStack stack) {
		RecipesForStack recipesForType = getRecipesForType(recipeType);
		List<Object> recipeInputList = recipesForType.getRecipes(stack);
		if (recipeInputList == null) {
			recipeInputList = new ArrayList<Object>();
			recipesForType.addRecipes(stack, recipeInputList);
		}
		return recipeInputList;
	}

	public void addRecipe(Object recipe, IRecipeType recipeType, List<ItemStack> itemStacks) {
		RecipesForStack recipesForType = getRecipesForType(recipeType);
		for (ItemStack itemStack : itemStacks) {
			recipesForType.getRecipes(itemStack).add(recipe);

			List<IRecipeType> recipeTypes = getRecipeTypes(itemStack);
			if (!recipeTypes.contains(recipeType))
				recipeTypes.add(recipeType);
		}
	}

	private class RecipesForStack {
		private final Map<String, List<Object>> map = new HashMap<String, List<Object>>();

		public List<Object> getRecipes(ItemStack itemStack) {
			String stackKey = asKey(itemStack);
			List<Object> recipeInputList = map.get(stackKey);
			if (recipeInputList == null) {
				recipeInputList = new ArrayList<Object>();
				map.put(stackKey, recipeInputList);
			}
			return recipeInputList;
		}

		public void addRecipes(ItemStack itemStack, List<Object> recipes) {
			String stackKey = asKey(itemStack);
			map.put(stackKey, recipes);
		}
	}

}
