package mezz.jei.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import mezz.jei.RecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;

/**
 * A RecipeMap efficiently links Recipes, IRecipeCategory, and ItemStacks.
 */
public class RecipeMap {

	@Nonnull
	private final Table<IRecipeCategory, String, List<Object>> recipeTable = HashBasedTable.create();
	@Nonnull
	private final ArrayListMultimap<String, IRecipeCategory> categoryMap = ArrayListMultimap.create();
	@Nonnull
	private final Ordering<IRecipeCategory> recipeCategoryOrdering;

	public RecipeMap(final RecipeRegistry recipeRegistry) {
		Comparator<IRecipeCategory> recipeCategoryComparator = new Comparator<IRecipeCategory>() {
			public int compare(IRecipeCategory recipeCategory1, IRecipeCategory recipeCategory2) {
				Integer index1 = recipeRegistry.getRecipeCategoryIndex(recipeCategory1);
				Integer index2 = recipeRegistry.getRecipeCategoryIndex(recipeCategory2);
				return index1.compareTo(index2);
			}
		};
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
	}

	@Nonnull
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nonnull ItemStack itemStack) {
		ImmutableSet.Builder<IRecipeCategory> recipeCategoriesBuilder = ImmutableSet.builder();
		for (String stackKey : getNamesWithWildcard(itemStack)) {
			recipeCategoriesBuilder.addAll(categoryMap.get(stackKey));
		}
		ImmutableSet<IRecipeCategory> recipeCategories = recipeCategoriesBuilder.build();
		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	private void addRecipeCategory(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack itemStack) {
		String stackKey = getName(itemStack);
		List<IRecipeCategory> recipeCategories = categoryMap.get(stackKey);
		if (!recipeCategories.contains(recipeCategory))
			recipeCategories.add(recipeCategory);
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
			return itemStack.getUnlocalizedName() + ':' + meta;
		}
	}

	@Nonnull
	private String getWildcardName(@Nonnull ItemStack itemStack) {
		return itemStack.getItem().getUnlocalizedName() + ':' + OreDictionary.WILDCARD_VALUE;
	}

	@Nonnull
	public ImmutableList<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack stack) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
		for (String name : getNamesWithWildcard(stack)) {
			List<Object> recipes = recipesForType.get(name);
			if (recipes != null)
				listBuilder.addAll(recipes);
		}
		return listBuilder.build();
	}

	public void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeCategory recipeCategory, @Nonnull Iterable<ItemStack> itemStacks) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null)
				continue;

			String stackKey = getName(itemStack);
			List<Object> recipes = recipesForType.get(stackKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(stackKey, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, itemStack);
		}
	}
}
