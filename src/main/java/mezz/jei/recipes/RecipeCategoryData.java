package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeCategoryData<T> {
	private final IRecipeCategory<T> recipeCategory;
	private final ImmutableList<Object> recipeCategoryCatalysts;
	private final List<T> recipes = new ArrayList<>();
	private final Set<T> hiddenRecipes = Collections.newSetFromMap(new IdentityHashMap<>());

	public RecipeCategoryData(IRecipeCategory<T> recipeCategory, ImmutableList<Object> recipeCategoryCatalysts) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryCatalysts = recipeCategoryCatalysts;
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	public ImmutableList<Object> getRecipeCategoryCatalysts() {
		return recipeCategoryCatalysts;
	}

	public List<T> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	public void addRecipes(Collection<T> recipes) {
		this.recipes.addAll(recipes);
	}

	public Set<T> getHiddenRecipes() {
		return hiddenRecipes;
	}
}
