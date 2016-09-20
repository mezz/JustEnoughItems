package mezz.jei.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * A RecipeMap efficiently links Recipes, IRecipeCategory, and ItemStacks.
 */
public class RecipeMap {

	private final Table<IRecipeCategory, String, List<Object>> recipeTable = HashBasedTable.create();
	private final ArrayListMultimap<String, IRecipeCategory> categoryMap = ArrayListMultimap.create();
	private final Ordering<IRecipeCategory> recipeCategoryOrdering;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
	}

	public ImmutableList<IRecipeCategory> getRecipeCategories(ItemStack itemStack) {
		Set<IRecipeCategory> recipeCategories = new HashSet<IRecipeCategory>();
		for (String stackKey : Internal.getStackHelper().getUniqueIdentifiersWithWildcard(itemStack)) {
			recipeCategories.addAll(categoryMap.get(stackKey));
		}
		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public ImmutableList<IRecipeCategory> getRecipeCategories(FluidStack fluid) {
		String key = getKeyForFluid(fluid);
		return recipeCategoryOrdering.immutableSortedCopy(categoryMap.get(key));
	}

	public void addRecipeCategory(IRecipeCategory recipeCategory, ItemStack itemStack) {
		String stackKey = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack);
		List<IRecipeCategory> recipeCategories = categoryMap.get(stackKey);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	private void addRecipeCategory(IRecipeCategory recipeCategory, FluidStack fluidStack) {
		String key = getKeyForFluid(fluidStack);
		List<IRecipeCategory> recipeCategories = categoryMap.get(key);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	private String getKeyForFluid(FluidStack fluidStack) {
		if (fluidStack.tag != null) {
			return "fluid:" + fluidStack.getFluid().getName() + ":" + fluidStack.tag;
		}
		return "fluid:" + fluidStack.getFluid().getName();
	}

	public ImmutableList<Object> getRecipes(IRecipeCategory recipeCategory, ItemStack stack) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
		for (String name : Internal.getStackHelper().getUniqueIdentifiersWithWildcard(stack)) {
			List<Object> recipes = recipesForType.get(name);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public List<Object> getRecipes(IRecipeCategory recipeCategory, FluidStack fluidStack) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		String name = getKeyForFluid(fluidStack);
		List<Object> recipes = recipesForType.get(name);
		if (recipes == null) {
			return ImmutableList.of();
		}
		return Collections.unmodifiableList(recipes);
	}

	public void addRecipe(Object recipe, IRecipeCategory recipeCategory, Iterable<ItemStack> itemStacks, Iterable<FluidStack> fluidStacks) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);
		StackHelper stackHelper = Internal.getStackHelper();

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}

			String stackKey = stackHelper.getUniqueIdentifierForStack(itemStack);
			List<Object> recipes = recipesForType.get(stackKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(stackKey, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, itemStack);
		}

		for (FluidStack fluidStack : fluidStacks) {
			if (fluidStack == null || fluidStack.getFluid() == null) {
				continue;
			}

			String fluidKey = getKeyForFluid(fluidStack);
			List<Object> recipes = recipesForType.get(fluidKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(fluidKey, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, fluidStack);
		}
	}
}
