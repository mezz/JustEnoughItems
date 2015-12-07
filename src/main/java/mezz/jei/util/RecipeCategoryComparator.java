package mezz.jei.util;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

import mezz.jei.api.recipe.IRecipeCategory;

public class RecipeCategoryComparator implements Comparator<IRecipeCategory> {
	@Nonnull
	private final ImmutableList<IRecipeCategory> recipeCategories;

	public RecipeCategoryComparator(@Nonnull List<IRecipeCategory> recipeCategories) {
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
	}

	@Override
	public int compare(IRecipeCategory recipeCategory1, IRecipeCategory recipeCategory2) {
		Integer index1 = recipeCategories.indexOf(recipeCategory1);
		Integer index2 = recipeCategories.indexOf(recipeCategory2);
		return index1.compareTo(index2);
	}
}
