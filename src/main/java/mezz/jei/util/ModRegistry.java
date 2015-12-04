package mezz.jei.util;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.RecipeRegistry;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;

public class ModRegistry implements IModRegistry {
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final List<IRecipeHandler> recipeHandlers = new ArrayList<>();
	private final List<IRecipeTransferHelper> recipeTransferHelpers = new ArrayList<>();
	private final List<Object> recipes = new ArrayList<>();

	@Override
	public void addRecipeCategories(@Nonnull IRecipeCategory... recipeCategories) {
		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public void addRecipeHandlers(@Nonnull IRecipeHandler... recipeHandlers) {
		Collections.addAll(this.recipeHandlers, recipeHandlers);
	}

	@Override
	public void addRecipeTransferHelpers(@Nonnull IRecipeTransferHelper... recipeTransferHelpers) {
		Collections.addAll(this.recipeTransferHelpers, recipeTransferHelpers);
	}

	@Override
	public void addRecipes(@Nonnull List<Object> recipes) {
		this.recipes.addAll(recipes);
	}

	public RecipeRegistry createRecipeRegistry() {
		return new RecipeRegistry(ImmutableList.copyOf(recipeCategories), ImmutableList.copyOf(recipeHandlers), ImmutableList.copyOf(recipeTransferHelpers), ImmutableList.copyOf(recipes));
	}
}
