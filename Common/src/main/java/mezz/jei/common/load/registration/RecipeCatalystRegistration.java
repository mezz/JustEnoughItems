package mezz.jei.common.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.ingredients.TypedIngredient;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.common.util.ErrorUtil;

public class RecipeCatalystRegistration implements IRecipeCatalystRegistration {
	private final ListMultiMap<ResourceLocation, ITypedIngredient<?>> recipeCatalysts = new ListMultiMap<>();
	private final IRegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final IJeiHelpers jeiHelpers;

	public RecipeCatalystRegistration(IRegisteredIngredients registeredIngredients, IIngredientManager ingredientManager, IJeiHelpers jeiHelpers) {
		this.registeredIngredients = registeredIngredients;
		this.ingredientManager = ingredientManager;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public <T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient, RecipeType<?>... recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		for (RecipeType<?> recipeType : recipeTypes) {
			ErrorUtil.checkNotNull(recipeType, "recipeType");
			ITypedIngredient<T> typedIngredient = TypedIngredient.createTyped(this.registeredIngredients, ingredientType, ingredient)
				.orElseThrow(() -> new IllegalArgumentException("Recipe catalyst must not be empty"));
			this.recipeCatalysts.put(recipeType.getUid(), typedIngredient);
		}
	}

	public ImmutableListMultimap<ResourceLocation, ITypedIngredient<?>> getRecipeCatalysts() {
		return recipeCatalysts.toImmutable();
	}
}
