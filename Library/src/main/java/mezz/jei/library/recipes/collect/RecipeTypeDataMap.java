package mezz.jei.library.recipes.collect;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeTypeDataMap {
	@Unmodifiable
	private final Map<ResourceLocation, RecipeTypeData<?>> uidMap;

	public RecipeTypeDataMap(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsMap
	) {
		this.uidMap = recipeCategories.stream()
			.collect(
				Collectors.toUnmodifiableMap(
					recipeCategory -> recipeCategory.getRecipeType().getUid(),
					recipeCategory -> {
						List<ITypedIngredient<?>> catalysts = recipeCategoryCatalystsMap.get(recipeCategory);
						return new RecipeTypeData<>(recipeCategory, catalysts);
					}
				)
			);
	}

	public <T> RecipeTypeData<T> get(RecipeType<T> recipeType) {
		RecipeTypeData<?> data = this.uidMap.get(recipeType.getUid());
		@SuppressWarnings("unchecked")
		RecipeTypeData<T> recipeTypeData = (RecipeTypeData<T>) data;
		return recipeTypeData;
	}

	public <T> RecipeTypeData<T> get(Iterable<? extends T> recipes, RecipeType<T> recipeType) {
		RecipeTypeData<T> recipeTypeData = get(recipeType);
		return validate(recipes, recipeTypeData);
	}

	private static <T> RecipeTypeData<T> validate(Iterable<? extends T> recipes, RecipeTypeData<?> recipeTypeData) {
		IRecipeCategory<?> recipeCategory = recipeTypeData.getRecipeCategory();
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		Class<?> recipeClass = recipeType.getRecipeClass();
		for (T recipe : recipes) {
			if (!recipeClass.isInstance(recipe)) {
				throw new IllegalArgumentException(recipeType.getUid() + " recipes must be an instance of " + recipeClass + ". Instead got: " + recipe.getClass());
			}
		}
		@SuppressWarnings("unchecked")
		RecipeTypeData<T> castRecipeTypeData = (RecipeTypeData<T>) recipeTypeData;
		return castRecipeTypeData;
	}

	public void validate(RecipeType<?> recipeType) {
		if (!uidMap.containsKey(recipeType.getUid())) {
			throw new IllegalStateException("There is no recipe type registered for: " + recipeType.getUid());
		}
	}

	public Optional<RecipeType<?>> getType(ResourceLocation recipeTypeUid) {
		RecipeTypeData<?> data = uidMap.get(recipeTypeUid);
		return Optional.ofNullable(data)
			.map(RecipeTypeData::getRecipeCategory)
			.map(IRecipeCategory::getRecipeType);
	}
}
