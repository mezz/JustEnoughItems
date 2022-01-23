package mezz.jei.recipes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableCollection;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientInformationUtil;

public class InternalRecipeManagerPlugin implements IRecipeManagerPlugin {
	private final ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys;
	private final IIngredientManager ingredientManager;
	private final RecipeCategoryDataMap recipeCategoriesMap;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;

	public InternalRecipeManagerPlugin(
		ImmutableMultimap<String, ResourceLocation> categoriesForRecipeCatalystKeys,
		IIngredientManager ingredientManager,
		RecipeCategoryDataMap recipeCategoriesMap,
		RecipeMap recipeInputMap,
		RecipeMap recipeOutputMap
	) {
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

		return switch (focus.getMode()) {
			case INPUT -> recipeInputMap.getRecipeCategories(ingredient);
			case OUTPUT -> recipeOutputMap.getRecipeCategories(ingredient);
		};
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		focus = Focus.check(focus);
		V ingredient = focus.getValue();

		if (focus.getMode() == IFocus.Mode.INPUT) {
			return getInputRecipes(recipeCategory, ingredient);
		} else {
			return recipeOutputMap.getRecipes(recipeCategory, ingredient);
		}
	}

	private <T, V> List<T> getInputRecipes(IRecipeCategory<T> recipeCategory, V ingredient) {
		final List<T> recipes = recipeInputMap.getRecipes(recipeCategory, ingredient);

		if (isCatalystIngredient(recipeCategory, ingredient)) {
			RecipeCategoryData<T> categoryData = recipeCategoriesMap.get(recipeCategory);
			List<T> recipesForCategory = categoryData.getRecipes();
			return Stream.concat(recipes.stream(), recipesForCategory.stream())
				.distinct()
				.toList();
		}

		return recipes;
	}

	private <T, V> boolean isCatalystIngredient(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		String ingredientUid = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
		ImmutableCollection<ResourceLocation> catalystCategories = categoriesForRecipeCatalystKeys.get(ingredientUid);
		return catalystCategories.contains(recipeCategoryUid);
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		RecipeCategoryData<T> recipeCategoryData = recipeCategoriesMap.get(recipeCategory);
		List<T> recipes = recipeCategoryData.getRecipes();
		return Collections.unmodifiableList(recipes);
	}
}
