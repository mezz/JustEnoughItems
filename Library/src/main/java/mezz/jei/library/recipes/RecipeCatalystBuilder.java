package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.recipes.collect.RecipeMap;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class RecipeCatalystBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, Consumer<IIngredientAcceptor<?>>> recipeCategoryCatalystsBuilder = ImmutableListMultimap.builder();
	private final RecipeMap recipeCatalystMap;

	public RecipeCatalystBuilder(RecipeMap recipeCatalystMap) {
		this.recipeCatalystMap = recipeCatalystMap;
	}

	public void addCategoryCatalysts(IRecipeCategory<?> recipeCategory, List<ITypedIngredient<?>> categoryCatalysts) {
		for (ITypedIngredient<?> catalyst : categoryCatalysts) {
			recipeCategoryCatalystsBuilder.put(recipeCategory, acceptor -> acceptor.addTypedIngredient(catalyst));
			addCategoryCatalyst(catalyst, recipeCategory);
		}
	}

	public void addCategoryCatalysts(
		IRecipeCategory<?> recipeCategory,
		List<Consumer<IIngredientAcceptor<?>>> categoryCatalysts,
		Function<Consumer<IIngredientAcceptor<?>>, Stream<ITypedIngredient<?>>> resolver
	) {
		recipeCategoryCatalystsBuilder.putAll(recipeCategory, categoryCatalysts);
		categoryCatalysts.stream()
			.flatMap(resolver)
			.forEach(catalyst -> addCategoryCatalyst(catalyst, recipeCategory));
	}

	private <T> void addCategoryCatalyst(ITypedIngredient<T> catalystIngredient, IRecipeCategory<?> recipeCategory) {
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		recipeCatalystMap.addCatalystForCategory(recipeType, catalystIngredient);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, Consumer<IIngredientAcceptor<?>>> buildRecipeCategoryCatalysts() {
		return recipeCategoryCatalystsBuilder.build();
	}
}
