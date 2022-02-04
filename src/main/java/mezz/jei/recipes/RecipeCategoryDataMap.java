package mezz.jei.recipes;

import java.util.List;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeCategoryDataMap {
	private final ImmutableMap<ResourceLocation, RecipeCategoryData<?>> map;

	public RecipeCategoryDataMap(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsMap
	) {
		ImmutableMap.Builder<ResourceLocation, RecipeCategoryData<?>> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			ImmutableList<ITypedIngredient<?>> recipeCategoryCatalysts = recipeCategoryCatalystsMap.get(recipeCategory);
			mapBuilder.put(recipeCategory.getUid(), new RecipeCategoryData<>(recipeCategory, recipeCategoryCatalysts));
		}
		this.map = mapBuilder.build();
	}

	public <T> RecipeCategoryData<T> get(IRecipeCategory<T> recipeCategory) {
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		@SuppressWarnings("unchecked")
		RecipeCategoryData<T> recipeCategoryData = (RecipeCategoryData<T>) get(recipeCategoryUid);
		return recipeCategoryData;
	}

	public <T> RecipeCategoryData<T> get(Iterable<T> recipes, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<?> recipeCategoryData = get(recipeCategoryUid);
		IRecipeCategory<?> recipeCategory = recipeCategoryData.getRecipeCategory();
		Class<?> recipeClass = recipeCategory.getRecipeClass();
		for (T recipe : recipes) {
			if (!recipeClass.isInstance(recipe)) {
				throw new IllegalArgumentException(recipeCategory.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
		}
		@SuppressWarnings("unchecked")
		RecipeCategoryData<T> castRecipeCategoryData = (RecipeCategoryData<T>) recipeCategoryData;
		return castRecipeCategoryData;
	}

	public <T> RecipeCategoryData<T> get(T recipe, ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<?> recipeCategoryData = get(recipeCategoryUid);
		IRecipeCategory<?> recipeCategory = recipeCategoryData.getRecipeCategory();
		Class<?> recipeClass = recipeCategory.getRecipeClass();
		if (!recipeClass.isInstance(recipe)) {
			throw new IllegalArgumentException(recipeCategory.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
		}
		@SuppressWarnings("unchecked")
		RecipeCategoryData<T> castRecipeCategoryData = (RecipeCategoryData<T>) recipeCategoryData;
		return castRecipeCategoryData;
	}

	public RecipeCategoryData<?> get(ResourceLocation recipeCategoryUid) {
		RecipeCategoryData<?> recipeCategoryData = map.get(recipeCategoryUid);
		if (recipeCategoryData == null) {
			throw new IllegalStateException("There is no recipe category registered for: " + recipeCategoryUid);
		}
		return recipeCategoryData;
	}

	public void validate(ResourceLocation recipeCategoryUid) {
		if (!map.containsKey(recipeCategoryUid)) {
			throw new IllegalStateException("There is no recipe category registered for: " + recipeCategoryUid);
		}
	}
}
