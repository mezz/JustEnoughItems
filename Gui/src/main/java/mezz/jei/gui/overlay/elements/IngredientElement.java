package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class IngredientElement<T> implements IElement<T> {
	private final ITypedIngredient<T> ingredient;

	public IngredientElement(ITypedIngredient<T> ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.empty();
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
	public List<Component> getTooltip(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		return tooltipHelper.getIngredientTooltip(ingredient, ingredientRenderer, ingredientHelper);
	}

	@Override
	public boolean isVisible() {
		return true;
	}
}
