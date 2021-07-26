package mezz.jei.recipes;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.recipe.category.IRecipeCategory;

public class RecipeCategoryDataMap {
	private final ImmutableMap<ResourceLocation, RecipeCategoryData<?>> map;

	public RecipeCategoryDataMap(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<IRecipeCategory<?>, Object> recipeCatalystsMap
	) {
		ImmutableMap.Builder<ResourceLocation, RecipeCategoryData<?>> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			ImmutableList<Object> recipeCatalysts = recipeCatalystsMap.get(recipeCategory);
			mapBuilder.put(recipeCategory.getUid(), new RecipeCategoryData<>(recipeCategory, recipeCatalysts));
		}
		this.map = mapBuilder.build();
	}

	public <T> RecipeCategoryData<T> get(IRecipeCategory<T> recipeCategory) {
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		@SuppressWarnings("unchecked")
		RecipeCategoryData<T> recipeCategoryData = (RecipeCategoryData<T>) get(recipeCategoryUid);
		return recipeCategoryData;
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
			throw new IllegalStateException("Failed to find recipe category data for: " + recipeCategoryUid);
		}
		return recipeCategoryData;
	}

	public void validate(ResourceLocation recipeCategoryUid) {
		if (!map.containsKey(recipeCategoryUid)) {
			throw new IllegalStateException("Failed to find recipe category data for: " + recipeCategoryUid);
		}
	}
}
