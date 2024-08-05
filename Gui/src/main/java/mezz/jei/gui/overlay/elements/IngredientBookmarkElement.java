package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Optional;

public class IngredientBookmarkElement<T> implements IElement<T> {
	private final IngredientBookmark<T> bookmark;

	public IngredientBookmarkElement(IngredientBookmark<T> bookmark) {
		this.bookmark = bookmark;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return bookmark.getIngredient();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(bookmark);
	}

	@Override
	public void renderExtras(GuiGraphics guiGraphics) {

	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, roles);
		recipesGui.show(focuses);
	}

	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		ITypedIngredient<T> ingredient = bookmark.getIngredient();
		tooltipHelper.getIngredientTooltip(tooltip, ingredient, ingredientRenderer, ingredientHelper);
	}

	@Override
	public boolean isVisible() {
		return bookmark.isVisible();
	}
}
