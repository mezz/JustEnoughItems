package mezz.jei.library.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeCatalystLookup;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeManager implements IRecipeManager {
	private final RecipeManagerInternal internal;
	private final IIngredientManager ingredientManager;

	public RecipeManager(RecipeManagerInternal internal, IIngredientManager ingredientManager) {
		this.internal = internal;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		return new RecipeLookup<>(recipeType, internal, ingredientManager);
	}

	@Override
	public IRecipeCategoriesLookup createRecipeCategoryLookup() {
		return new RecipeCategoriesLookup(internal, ingredientManager);
	}

	@Override
	public <T> IRecipeCategory<T> getRecipeCategory(RecipeType<T> recipeType) {
		return internal.getRecipeCategory(recipeType);
	}

	@Override
	public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
		return new RecipeCatalystLookup(recipeType, internal);
	}

	@Override
	public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipes, "recipes");
		ErrorUtil.validateRecipes(recipeType, recipes);
		ErrorUtil.assertMainThread();

		internal.addRecipes(recipeType, recipes);
	}

	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focusGroup) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");

		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Collection<IRecipeCategoryDecorator<T>> decorators = internal.getRecipeCategoryDecorators(recipeType);
		return RecipeLayout.create(
			recipeCategory,
			decorators,
			recipe,
			focusGroup,
			ingredientManager
		);
	}

	@Override
	public <T> Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable(
		IRecipeCategory<T> recipeCategory,
		T recipe,
		IFocusGroup focusGroup,
		IScalableDrawable background,
		int borderSize
	) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotNull(recipe, "recipe");
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		ErrorUtil.checkNotNull(background, "background");

		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Collection<IRecipeCategoryDecorator<T>> decorators = internal.getRecipeCategoryDecorators(recipeType);
		return RecipeLayout.create(
			recipeCategory,
			decorators,
			recipe,
			focusGroup,
			ingredientManager,
			background,
			borderSize
		);
	}

	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(RecipeIngredientRole role, List<Optional<ITypedIngredient<?>>> ingredients, Set<Integer> focusedIngredients, int xPos, int yPos, int ingredientCycleOffset) {
		RecipeSlotBuilder builder = new RecipeSlotBuilder(ingredientManager, role, xPos, yPos, ingredientCycleOffset);
		builder.addOptionalTypedIngredients(ingredients);
		return builder.build(focusedIngredients);
	}

	@Override
	public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		ErrorUtil.checkNotNull(recipes, "recipe");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.validateRecipes(recipeType, recipes);
		ErrorUtil.assertMainThread();
		internal.hideRecipes(recipeType, recipes);
	}

	@Override
	public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
		ErrorUtil.checkNotNull(recipes, "recipe");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.validateRecipes(recipeType, recipes);
		ErrorUtil.assertMainThread();
		internal.unhideRecipes(recipeType, recipes);
	}

	@Override
	public void hideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.hideRecipeCategory(recipeType);
	}

	@Override
	public void unhideRecipeCategory(RecipeType<?> recipeType) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.assertMainThread();
		internal.unhideRecipeCategory(recipeType);
	}

	@Override
	public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
		return internal.getRecipeType(recipeUid);
	}
}
