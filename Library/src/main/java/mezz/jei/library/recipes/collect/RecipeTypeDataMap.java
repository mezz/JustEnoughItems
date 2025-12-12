package mezz.jei.library.recipes.collect;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeTypeDataMap {
	@Unmodifiable
	private final Map<IRecipeType<?>, RecipeTypeData<?>> uidMap;

	public RecipeTypeDataMap(
		List<IRecipeCategory<?>> recipeCategories,
		ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> craftingStationMap
	) {
		this.uidMap = recipeCategories.stream()
			.collect(
				Collectors.toUnmodifiableMap(
					IRecipeCategory::getRecipeType,
					recipeCategory -> {
						List<ITypedIngredient<?>> craftingStations = craftingStationMap.get(recipeCategory);
						return new RecipeTypeData<>(recipeCategory, craftingStations);
					}
				)
			);
	}

	public <T> RecipeTypeData<T> get(IRecipeType<T> recipeType) {
		RecipeTypeData<?> data = this.uidMap.get(recipeType);
		if (data == null) {
			throw new IllegalStateException(
				"There is no recipe category registered for: " + recipeType +
				"\nA recipe category must be registered in order to use this recipe type."
			);
		}
		@SuppressWarnings("unchecked")
		RecipeTypeData<T> recipeTypeData = (RecipeTypeData<T>) data;
		return recipeTypeData;
	}

	public void validate(IRecipeType<?> recipeType) {
		if (!uidMap.containsKey(recipeType)) {
			throw new IllegalStateException("There is no recipe type registered for: " + recipeType);
		}
	}

	public Optional<IRecipeType<?>> getType(Identifier recipeTypeUid) {
		return uidMap.keySet()
			.stream()
			.filter(recipeType -> recipeType.getUid().equals(recipeTypeUid))
			.findFirst();
	}

	public <T> Optional<IRecipeType<T>> getType(Identifier recipeTypeUid, Class<? extends T> recipeClass) {
		return uidMap.keySet()
			.stream()
			.filter(recipeType -> recipeType.getUid().equals(recipeTypeUid) && recipeType.getRecipeClass().equals(recipeClass))
			.map(recipeType -> {
				@SuppressWarnings("unchecked")
				IRecipeType<T> castRecipeType = (IRecipeType<T>) recipeType;
				return castRecipeType;
			})
			.findFirst();
	}
}
