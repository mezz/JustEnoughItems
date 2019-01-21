package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeCategoryComparator implements Comparator<ResourceLocation> {
	private final ImmutableList<ResourceLocation> recipeCategories;

	public RecipeCategoryComparator(List<IRecipeCategory> recipeCategories) {
		List<ResourceLocation> recipeCategoryUids = new ArrayList<>();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			recipeCategoryUids.add(recipeCategory.getUid());
		}
		this.recipeCategories = ImmutableList.copyOf(recipeCategoryUids);
	}

	@Override
	public int compare(ResourceLocation recipeCategory1, ResourceLocation recipeCategory2) {
		Integer index1 = recipeCategories.indexOf(recipeCategory1);
		Integer index2 = recipeCategories.indexOf(recipeCategory2);
		return index1.compareTo(index2);
	}
}
