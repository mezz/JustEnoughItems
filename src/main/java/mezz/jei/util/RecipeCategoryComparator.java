package mezz.jei.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeCategory;

public class RecipeCategoryComparator implements Comparator<String> {
	private final ImmutableList<String> recipeCategories;

	public RecipeCategoryComparator(List<IRecipeCategory> recipeCategories) {
		List<String> recipeCategoryUids = new ArrayList<String>();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			recipeCategoryUids.add(recipeCategory.getUid());
		}
		this.recipeCategories = ImmutableList.copyOf(recipeCategoryUids);
	}

	@Override
	public int compare(String recipeCategory1, String recipeCategory2) {
		Integer index1 = recipeCategories.indexOf(recipeCategory1);
		Integer index2 = recipeCategories.indexOf(recipeCategory2);
		return index1.compareTo(index2);
	}
}
