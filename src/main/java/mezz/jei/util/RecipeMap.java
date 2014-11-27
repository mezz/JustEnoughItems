package mezz.jei.util;

import mezz.jei.api.recipe.IRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A RecipeMap efficiently links Recipes, IRecipeTypes, and ItemStacks.
 */
public class RecipeMap {

	@Nonnull
	private final Map<IRecipeType, RecipesForType> recipeMap = new HashMap<IRecipeType, RecipesForType>();
	@Nonnull
	private final Map<String, List<IRecipeType>> typeMap = new HashMap<String, List<IRecipeType>>();

	@Nonnull
	private RecipesForType getRecipesForType(IRecipeType recipeType) {
		RecipesForType recipesForType = recipeMap.get(recipeType);
		if (recipesForType == null) {
			recipesForType = new RecipesForType();
			recipeMap.put(recipeType, recipesForType);
		}
		return recipesForType;
	}

	@Nonnull
	public List<IRecipeType> getRecipeTypes(@Nonnull ItemStack itemStack) {
		Set<IRecipeType> recipeTypes = new LinkedHashSet<IRecipeType>();
		for (String stackKey : getNamesWithWildcard(itemStack)) {
			recipeTypes.addAll(getRecipeTypes(stackKey));
		}
		return new ArrayList<IRecipeType>(recipeTypes);
	}

	private void addRecipeType(@Nonnull IRecipeType recipeType, @Nonnull ItemStack itemStack) {
		String stackKey = getName(itemStack);
		List<IRecipeType> recipeTypes = getRecipeTypes(stackKey);
		if (!recipeTypes.contains(recipeType))
			recipeTypes.add(recipeType);
	}

	private List<IRecipeType> getRecipeTypes(String stackKey) {
		List<IRecipeType> recipeTypes = typeMap.get(stackKey);
		if (recipeTypes == null) {
			recipeTypes = new ArrayList<IRecipeType>();
			typeMap.put(stackKey, recipeTypes);
		}
		return recipeTypes;
	}

	@Nonnull
	private List<String> getNamesWithWildcard(@Nonnull ItemStack itemStack) {
		List<String> names = new ArrayList<String>(2);
		names.add(getName(itemStack));
		names.add(getWildcardName(itemStack));
		return names;
	}

	@Nonnull
	private String getName(@Nonnull ItemStack itemStack) {
		int meta = itemStack.getItemDamage();
		if (meta == OreDictionary.WILDCARD_VALUE) {
			return getWildcardName(itemStack);
		} else {
			return itemStack.getUnlocalizedName() + ":" + meta;
		}
	}

	@Nonnull
	private String getWildcardName(@Nonnull ItemStack itemStack) {
		return itemStack.getItem().getUnlocalizedName() + ":" + OreDictionary.WILDCARD_VALUE;
	}

	@Nonnull
	public List<Object> getRecipes(@Nonnull IRecipeType recipeType, @Nonnull ItemStack stack) {
		RecipesForType recipesForType = getRecipesForType(recipeType);
		return recipesForType.getRecipes(stack);
	}

	public void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeType recipeType, @Nonnull Iterable<ItemStack> itemStacks) {
		RecipesForType recipesForType = getRecipesForType(recipeType);
		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null)
				continue;

			recipesForType.addRecipe(itemStack, recipe);

			addRecipeType(recipeType, itemStack);
		}
	}

	private class RecipesForType {
		@Nonnull
		private final Map<String, List<Object>> map = new HashMap<String, List<Object>>();

		public void addRecipe(@Nonnull ItemStack itemStack, @Nonnull Object recipe) {
			String name = getName(itemStack);
			getRecipes(name).add(recipe);
		}

		@Nonnull
		public List<Object> getRecipes(@Nonnull ItemStack itemStack) {
			Set<Object> recipes = new LinkedHashSet<Object>();
			for (String name : getNamesWithWildcard(itemStack)) {
				recipes.addAll(getRecipes(name));
			}
			return new ArrayList<Object>(recipes);
		}

		private List<Object> getRecipes(String stackKey) {
			List<Object> recipes = map.get(stackKey);
			if (recipes == null) {
				recipes = new ArrayList<Object>();
				map.put(stackKey, recipes);
			}
			return recipes;
		}
	}

}
