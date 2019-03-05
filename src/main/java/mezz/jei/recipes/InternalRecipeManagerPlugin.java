package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientInformation;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	private final RecipeManager recipeManager;
	private final ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys;
	private final IIngredientManager ingredientManager;
	private final RecipeCategoryDataMap recipeCategoriesMap;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;

	public InternalRecipeManagerPlugin(
		RecipeManager recipeManager,
		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys,
		IIngredientManager ingredientManager,
		RecipeCategoryDataMap recipeCategoriesMap,
		RecipeMap recipeInputMap,
		RecipeMap recipeOutputMap
	) {
		this.recipeManager = recipeManager;
		this.categoriesForRecipeCatalystKeys = categoriesForRecipeCatalystKeys;
		this.ingredientManager = ingredientManager;
		this.recipeCategoriesMap = recipeCategoriesMap;
		this.recipeInputMap = recipeInputMap;
		this.recipeOutputMap = recipeOutputMap;
	}

	@Override
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		focus = Focus.check(focus);
		V ingredient = focus.getValue();

		switch (focus.getMode()) {
			case INPUT:
				return recipeInputMap.getRecipeCategories(ingredient);
			case OUTPUT:
				return recipeOutputMap.getRecipeCategories(ingredient);
			default:
				return getRecipeCategories();
		}
	}

	private ImmutableList<ResourceLocation> getRecipeCategories() {
		ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
		for (IRecipeCategory<?> recipeCategory : recipeManager.getRecipeCategories()) {
			builder.add(recipeCategory.getUid());
		}
		return builder.build();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		focus = Focus.check(focus);
		V ingredient = focus.getValue();

		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		if (focus.getMode() == IFocus.Mode.INPUT) {
			final ImmutableList<T> recipes = recipeInputMap.getRecipes(recipeCategory, ingredient);

			ResourceLocation recipeCategoryUid = recipeCategory.getUid();
			for (String inputKey : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
				if (categoriesForRecipeCatalystKeys.get(inputKey).contains(recipeCategoryUid)) {
					ImmutableSet<T> specificRecipes = ImmutableSet.copyOf(recipes);
					RecipeCategoryData<T> recipeCategoryData = recipeCategoriesMap.get(recipeCategory);
					List<T> recipesForCategory = recipeCategoryData.getRecipes();
					List<T> allRecipes = new ArrayList<>(recipes);
					for (T recipe : recipesForCategory) {
						if (!specificRecipes.contains(recipe)) {
							allRecipes.add(recipe);
						}
					}
					return allRecipes;
				}
			}

			return recipes;
		} else {
			return recipeOutputMap.getRecipes(recipeCategory, ingredient);
		}
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesMap.get(recipeCategory);
		List<T> recipes = recipeCategoryData.getRecipes();
		return Collections.unmodifiableList(recipes);
	}
}
