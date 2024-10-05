package mezz.jei.gui.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RecipeBookmark<R, I> implements IBookmark {
	private final IElement<I> element;
	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final ResourceLocation recipeUid;
	private final ITypedIngredient<I> displayIngredient;
	private final boolean displayIsOutput;
	private boolean visible = true;

	@Nullable
	public static <T> RecipeBookmark<T, ?> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		IIngredientManager ingredientManager
	) {
		T recipe = recipeLayoutDrawable.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeLayoutDrawable.getRecipeCategory();
		ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
		if (recipeUid == null) {
			return null;
		}

		IRecipeSlotsView recipeSlotsView = recipeLayoutDrawable.getRecipeSlotsView();
		{
			ITypedIngredient<?> output = findFirst(recipeSlotsView, RecipeIngredientRole.OUTPUT);
			if (output != null) {
				output = ingredientManager.normalizeTypedIngredient(output);
				return new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output, true);
			}
		}

		{
			ITypedIngredient<?> input = findFirst(recipeSlotsView, RecipeIngredientRole.INPUT);
			if (input != null) {
				input = ingredientManager.normalizeTypedIngredient(input);
				return new RecipeBookmark<>(recipeCategory, recipe, recipeUid, input, false);
			}
		}

		return null;
	}

	@Nullable
	private static ITypedIngredient<?> findFirst(IRecipeSlotsView slotsView, RecipeIngredientRole role) {
		for (IRecipeSlotView slotView : slotsView.getSlotViews()) {
			if (slotView.getRole() != role) {
				continue;
			}
			for (ITypedIngredient<?> ingredient : slotView.getAllIngredientsList()) {
				if (ingredient != null) {
					return ingredient;
				}
			}
		}
		return null;
	}

	public RecipeBookmark(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		ResourceLocation recipeUid,
		ITypedIngredient<I> displayIngredient,
		boolean displayIsOutput
	) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.recipeUid = recipeUid;
		this.displayIngredient = displayIngredient;
		this.element = new RecipeBookmarkElement<>(this);
		this.displayIsOutput = displayIsOutput;
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

	public ITypedIngredient<I> getDisplayIngredient() {
		return displayIngredient;
	}

	public boolean isDisplayIsOutput() {
		return displayIsOutput;
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
			", displayIngredient=" + displayIngredient +
			", visible=" + visible +
			'}';
	}

	public <T> boolean isRecipe(RecipeType<T> otherType, T otherRecipe) {
		RecipeType<R> recipeType = recipeCategory.getRecipeType();
		if (recipeType.equals(otherType)) {
			Class<? extends R> recipeClass = recipeType.getRecipeClass();
			if (recipeClass.isInstance(otherRecipe)) {
				R castRecipe = recipeClass.cast(otherRecipe);
				ResourceLocation otherUid = recipeCategory.getRegistryName(castRecipe);
				return recipeUid.equals(otherUid);
			}
		}
		return false;
	}
}
