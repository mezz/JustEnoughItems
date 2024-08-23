package mezz.jei.gui.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecipeBookmark<R, I> implements IBookmark {
	private final IElement<I> element;
	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final ResourceLocation recipeUid;
	private final ITypedIngredient<I> recipeOutput;
	private boolean visible = true;

	public static <T> Optional<RecipeBookmark<T, ?>> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		IIngredientManager ingredientManager
	) {
		T recipe = recipeLayoutDrawable.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeLayoutDrawable.getRecipeCategory();
		ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
		if (recipeUid == null) {
			return Optional.empty();
		}

		IRecipeSlotsView recipeSlotsView = recipeLayoutDrawable.getRecipeSlotsView();
		List<IRecipeSlotView> outputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.OUTPUT);
		for (IRecipeSlotView slotView : outputSlots) {
			Optional<ITypedIngredient<?>> outputOptional = slotView.getAllIngredients().findFirst();
			if (outputOptional.isEmpty()) {
				continue;
			}
			ITypedIngredient<?> output = outputOptional.get();
			output = ingredientManager.normalizeTypedIngredient(output);
			return Optional.of(new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output));
		}
		return Optional.empty();
	}

	public RecipeBookmark(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		ResourceLocation recipeUid,
		ITypedIngredient<I> recipeOutput
	) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.recipeUid = recipeUid;
		this.recipeOutput = recipeOutput;
		this.element = new RecipeBookmarkElement<>(this);
	}

	@Override
	public BookmarkType getType() {
		return BookmarkType.RECIPE;
	}

	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	public R getRecipe() {
		return recipe;
	}

	public ITypedIngredient<I> getRecipeOutput() {
		return recipeOutput;
	}

	@Override
	public IElement<?> getElement() {
		return element;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipeUid, recipeCategory.getRecipeType());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecipeBookmark<?, ?> recipeBookmark) {
			return recipeBookmark.recipeUid.equals(recipeUid) &&
				recipeCategory.getRecipeType().equals(recipeBookmark.recipeCategory.getRecipeType());
		}
		return false;
	}

	@Override
	public String toString() {
		return "RecipeBookmark{" +
			"recipeCategory=" + recipeCategory.getRecipeType() +
			", recipe=" + recipe +
			", recipeUid=" + recipeUid +
			", recipeOutput=" + recipeOutput +
			", visible=" + visible +
			'}';
	}
}
