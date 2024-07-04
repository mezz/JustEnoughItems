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
		if (data == null) {
			throw new IllegalStateException(
				"There is no recipe category registered for: " + recipeType.getUid() +
				"\nA recipe category must be registered in order to use this recipe type."
			);
		}
		@SuppressWarnings("unchecked")
		RecipeTypeData<T> recipeTypeData = (RecipeTypeData<T>) data;
		return recipeTypeData;
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
