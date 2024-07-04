package mezz.jei.gui.overlay.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.ingredients.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<T, R> implements IElement<R> {
	private final RecipeBookmark<T, R> recipeBookmark;
	private final IDrawable icon;

	public RecipeBookmarkElement(RecipeBookmark<T, R> recipeBookmark, IDrawable icon) {
		this.recipeBookmark = recipeBookmark;
		this.icon = icon;
	}

	@Override
	public ITypedIngredient<R> getTypedIngredient() {
		return recipeBookmark.getRecipeOutput();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public @Nullable IDrawable createRenderOverlay() {
		return new RecipeBookmarkOverlay(icon);
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		// ignore roles, always display the bookmarked recipe if it's clicked

		IRecipeCategory<T> recipeCategory = recipeBookmark.getRecipeCategory();
		T recipe = recipeBookmark.getRecipe();
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, List.of(RecipeIngredientRole.OUTPUT));
		recipesGui.showRecipes(recipeCategory, List.of(recipe), focuses);
	}

	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<R> ingredientRenderer, IIngredientHelper<R> ingredientHelper) {
		ITypedIngredient<R> ingredient = recipeBookmark.getRecipeOutput();
		tooltipHelper.getRecipeTooltip(
			tooltip,
			recipeBookmark.getRecipeCategory(),
			recipeBookmark.getRecipe(),
			ingredient,
			ingredientRenderer,
			ingredientHelper
		);
	}

	@Override
	public boolean isVisible() {
		return recipeBookmark.isVisible();
	}

	private record RecipeBookmarkOverlay(IDrawable icon) implements IDrawable {
		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(PoseStack poseStack, int xOffset, int yOffset) {
			poseStack.pushPose();
			{
				// this z level seems to be the sweet spot so that
				// 2D icons draw above the items, and
				// 3D icons draw still draw under tooltips.
				poseStack.translate(xOffset + 8, yOffset + 8, 200);
				poseStack.scale(0.5f, 0.5f, 0.5f);
				icon.draw(poseStack);
			}
			poseStack.popPose();
		}
	}
}
