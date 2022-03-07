package mezz.jei.load.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.util.ErrorUtil;

public class RecipeCategoryRegistration implements IRecipeCategoryRegistration {
	private final List<IRecipeCategory<?>> recipeCategories = new ArrayList<>();
	private final Set<RecipeType<?>> recipeTypes = new HashSet<>();
	private final IJeiHelpers jeiHelpers;

	public RecipeCategoryRegistration(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public void addRecipeCategories(IRecipeCategory<?>... recipeCategories) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			RecipeType<?> recipeType = recipeCategory.getRecipeType();
			Preconditions.checkNotNull(recipeType, "Recipe type cannot be null %s", recipeCategory);
			if (recipeTypes.contains(recipeType)) {
				throw new IllegalArgumentException("A RecipeCategory with type \"" + recipeType.getRecipeClass() + "\" has already been registered.");
			} else {
				recipeTypes.add(recipeType);
			}
		}

		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	public ImmutableList<IRecipeCategory<?>> getRecipeCategories() {
		return ImmutableList.copyOf(recipeCategories);
	}
}
