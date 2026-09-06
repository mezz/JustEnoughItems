package mezz.jei.library.recipes;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeCatalystLookup;
import mezz.jei.api.recipe.RecipeType;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RecipeCatalystLookup implements IRecipeCatalystLookup {
	private final RecipeType<?> recipeType;
	private final RecipeManagerInternal recipeManager;
	private boolean includeHidden;

	public RecipeCatalystLookup(RecipeType<?> recipeType, RecipeManagerInternal recipeManager) {
		this.recipeType = recipeType;
		this.recipeManager = recipeManager;
	}

	@Override
	public IRecipeCatalystLookup includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<ITypedIngredient<?>> get() {
		return recipeManager.getRecipeCatalystGroups(recipeType, true)
			.flatMap(group -> recipeManager.getRecipeCatalystIngredients(group, includeHidden));
	}

	@Override
	public Stream<Consumer<IIngredientAcceptor<?>>> getGroups() {
		return recipeManager.getRecipeCatalystGroups(recipeType, includeHidden);
	}

	@Override
	public <V> Stream<V> get(IIngredientType<V> ingredientType) {
		return get()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}
}
