package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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
	public void renderExtras(GuiGraphics guiGraphics) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			// this z level seems to be the sweet spot so that
			// 2D icons draw above the items, and
			// 3D icons draw still draw under tooltips.
			poseStack.translate(8, 8, 200);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			icon.draw(guiGraphics);
		}
		poseStack.popPose();
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
	public List<Component> getTooltip(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<R> ingredientRenderer, IIngredientHelper<R> ingredientHelper) {
		ITypedIngredient<R> ingredient = recipeBookmark.getRecipeOutput();
		return tooltipHelper.getRecipeTooltip(
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
}
