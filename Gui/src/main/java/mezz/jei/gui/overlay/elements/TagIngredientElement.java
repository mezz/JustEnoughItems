package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.recipes.TagRecipeUtil;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class TagIngredientElement<T> extends IngredientElement<T> {
	private final TagKey<?> tagKey;
	private final IRecipeManager recipeManager;
	private final BooleanSupplier isRecipeCyclingPaused;

	public TagIngredientElement(
		ITypedIngredient<T> ingredient,
		TagKey<?> tagKey,
		IRecipeManager recipeManager,
		BooleanSupplier isRecipeCyclingPaused
	) {
		super(ingredient);
		this.tagKey = tagKey;
		this.recipeManager = recipeManager;
		this.isRecipeCyclingPaused = isRecipeCyclingPaused;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		if (!isRecipeCyclingPaused.getAsBoolean() &&
			roles.contains(RecipeIngredientRole.OUTPUT) &&
			showTagRecipe(recipesGui)
		) {
			return;
		}
		super.show(recipesGui, focusUtil, roles);
	}

	private boolean showTagRecipe(IRecipesGui recipesGui) {
		return recipeManager.getRecipeType(TagRecipeUtil.getRecipeTypeUid(tagKey))
			.map(recipeType -> showTagRecipe(recipesGui, recipeType))
			.orElse(false);
	}

	private <R> boolean showTagRecipe(IRecipesGui recipesGui, IRecipeType<R> recipeType) {
		boolean categoryVisible = recipeManager.createRecipeCategoryLookup()
			.limitTypes(List.of(recipeType))
			.get()
			.findAny()
			.isPresent();
		if (!categoryVisible) {
			return false;
		}

		IRecipeCategory<R> recipeCategory = recipeManager.getRecipeCategory(recipeType);
		Optional<R> recipe = findTagRecipe(
			tagKey,
			recipeCategory,
			recipeManager.createRecipeLookup(recipeType).get()
		);
		return recipe
			.map(r -> {
				recipesGui.showRecipes(recipeCategory, List.of(r), List.of());
				return true;
			})
			.orElse(false);
	}

	static <R> Optional<R> findTagRecipe(TagKey<?> tagKey, IRecipeCategory<R> recipeCategory, Stream<R> recipes) {
		return recipes
			.filter(recipe -> tagKey.location().equals(recipeCategory.getIdentifier(recipe)))
			.findFirst();
	}
}
