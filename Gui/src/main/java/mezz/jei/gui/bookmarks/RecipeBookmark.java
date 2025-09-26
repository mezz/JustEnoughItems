package mezz.jei.gui.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.gui.recipes.RecipeCategoryIconUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RecipeBookmark<R, I> implements IBookmark {
	private final IElement<I> element;
	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final ResourceLocation recipeUid;
	private final ITypedIngredient<I> recipeOutput;
	private final boolean displayIsOutput;
	private boolean visible = true;

	public static <T> Optional<RecipeBookmark<T, ?>> create(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		IIngredientManager ingredientManager,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		T recipe = recipeLayoutDrawable.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeLayoutDrawable.getRecipeCategory();
		ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
		if (recipeUid == null) {
			return Optional.empty();
		}

		IDrawable icon = RecipeCategoryIconUtil.create(
			recipeCategory,
			recipeManager,
			guiHelper
		);

		IRecipeSlotsView recipeSlotsView = recipeLayoutDrawable.getRecipeSlotsView();
		{
			ITypedIngredient<?> output = findFirst(recipeSlotsView, RecipeIngredientRole.OUTPUT);
			if (output != null) {
				output = ingredientManager.normalizeTypedIngredient(output);
				return Optional.of(new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output, icon, false));
			}
		}
		{
			ITypedIngredient<?> input = findFirst(recipeSlotsView, RecipeIngredientRole.INPUT);
			if (input != null) {
				input = ingredientManager.normalizeTypedIngredient(input);
				return Optional.of(new RecipeBookmark<>(recipeCategory, recipe, recipeUid, input, icon, false));
			}
		}

		return Optional.empty();
	}

	@Nullable
	private static ITypedIngredient<?> findFirst(IRecipeSlotsView slotsView, RecipeIngredientRole role) {
		for (IRecipeSlotView slotView : slotsView.getSlotViews()) {
			if (slotView.getRole() != role) {
				continue;
			}
			Optional<ITypedIngredient<?>> outputOptional = slotView.getAllIngredients().findFirst();
			if (outputOptional.isPresent()) {
				return outputOptional.get();
			}
		}
		return null;
	}

	public RecipeBookmark(
		IRecipeCategory<R> recipeCategory,
		R recipe,
		ResourceLocation recipeUid,
		ITypedIngredient<I> recipeOutput,
		IDrawable icon,
		boolean displayIsOutput
	) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.recipeUid = recipeUid;
		this.recipeOutput = recipeOutput;
		this.element = new RecipeBookmarkElement<>(this, icon);
		this.displayIsOutput = displayIsOutput;
	}

	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	public ResourceLocation getRecipeUid() {
		return recipeUid;
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

	public boolean isDisplayIsOutput() {
		return displayIsOutput;
	}

	@Override
	public int hashCode() {
		return recipeUid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecipeBookmark<?, ?> recipeBookmark) {
			return recipeBookmark.recipeUid.equals(recipeUid);
		}
		return false;
	}
}
