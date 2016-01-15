package mezz.jei.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.Internal;
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

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
	}

	@Nonnull
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nonnull ItemStack itemStack) {
		Set<IRecipeCategory> recipeCategories = new HashSet<>();
		for (String stackKey : Internal.getStackHelper().getUniqueIdentifiersWithWildcard(itemStack)) {
			recipeCategories.addAll(categoryMap.get(stackKey));
		}
		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	@Nonnull
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nonnull Fluid fluid) {
		String key = getKeyForFluid(fluid);
		return recipeCategoryOrdering.immutableSortedCopy(categoryMap.get(key));
	}

	private void addRecipeCategory(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack itemStack) {
		String stackKey = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack);
		List<IRecipeCategory> recipeCategories = categoryMap.get(stackKey);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	private void addRecipeCategory(@Nonnull IRecipeCategory recipeCategory, @Nonnull Fluid fluid) {
		String key = getKeyForFluid(fluid);
		List<IRecipeCategory> recipeCategories = categoryMap.get(key);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	@Nonnull
	private String getKeyForFluid(Fluid fluid) {
		return "fluid:" + fluid.getName();
	}

	@Nonnull
	public ImmutableList<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack stack) {
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

	@Nonnull
	public List<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory, @Nonnull Fluid fluid) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		String name = getKeyForFluid(fluid);
		List<Object> recipes = recipesForType.get(name);
		if (recipes == null) {
			return ImmutableList.of();
		}
		return Collections.unmodifiableList(recipes);
	}

	public void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeCategory recipeCategory, @Nonnull Iterable<ItemStack> itemStacks, @Nonnull Iterable<FluidStack> fluidStacks) {
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
			if (fluidStack == null) {
				continue;
			}
			Fluid fluid = fluidStack.getFluid();
			if (fluid == null) {
				continue;
			}

			String fluidKey = getKeyForFluid(fluid);
			List<Object> recipes = recipesForType.get(fluidKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(fluidKey, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, fluid);
		}
	}
}
