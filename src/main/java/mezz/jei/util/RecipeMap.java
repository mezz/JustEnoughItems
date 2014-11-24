package mezz.jei.util;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.api.recipe.IRecipeType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A RecipeMap efficiently links Recipes, IRecipeTypes, and ItemStacks.
 */
public class RecipeMap {

	@Nonnull
	private final Map<IRecipeType, RecipesForStack> recipeMap = new HashMap<IRecipeType, RecipesForStack>();
	@Nonnull
	private final Map<String, List<IRecipeType>> typeMap = new HashMap<String, List<IRecipeType>>();

	@Nonnull
	private static String asKey(@Nonnull ItemStack itemstack) {
		return itemstack.getUnlocalizedName() + ":" + GameData.getItemRegistry().getId(itemstack.getItem());
	}

	@Nonnull
	private RecipesForStack getRecipesForType(IRecipeType recipeType) {
		RecipesForStack recipesForStack = recipeMap.get(recipeType);
		if (recipesForStack == null) {
			recipesForStack = new RecipesForStack();
			recipeMap.put(recipeType, recipesForStack);
		}
		return recipesForStack;
	}

	@Nonnull
	public List<IRecipeType> getRecipeTypes(@Nonnull ItemStack itemStack) {
		String stackKey = asKey(itemStack);
		List<IRecipeType> recipeTypes = typeMap.get(stackKey);
		if (recipeTypes == null) {
			recipeTypes = new ArrayList<IRecipeType>();
			typeMap.put(stackKey, recipeTypes);
		}
		return recipeTypes;
	}

	@Nonnull
	public List<Object> getRecipes(@Nonnull IRecipeType recipeType, @Nonnull ItemStack stack) {
		RecipesForStack recipesForType = getRecipesForType(recipeType);
		return recipesForType.getRecipes(stack);
	}

	public void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeType recipeType, @Nonnull Iterable<ItemStack> itemStacks) {
		RecipesForStack recipesForType = getRecipesForType(recipeType);
		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null)
				continue;

			recipesForType.getRecipes(itemStack).add(recipe);

			List<IRecipeType> recipeTypes = getRecipeTypes(itemStack);
			if (!recipeTypes.contains(recipeType))
				recipeTypes.add(recipeType);
		}
	}

	private class RecipesForStack {
		@Nonnull
		private final Map<String, List<Object>> map = new HashMap<String, List<Object>>();

		@Nonnull
		public List<Object> getRecipes(@Nonnull ItemStack itemStack) {
			String stackKey = asKey(itemStack);
			List<Object> recipeInputList = map.get(stackKey);
			if (recipeInputList == null) {
				recipeInputList = new ArrayList<Object>();
				map.put(stackKey, recipeInputList);
			}
			return recipeInputList;
		}

		public void addRecipes(@Nonnull ItemStack itemStack, List<Object> recipes) {
			String stackKey = asKey(itemStack);
			map.put(stackKey, recipes);
		}
	}

}
