package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Optional;

public interface IElement<T> {
	ITypedIngredient<T> getTypedIngredient();

	/**
	 * @return the bookmark if this element represents an existing bookmark.
	 */
	Optional<IBookmark> getBookmark();

	void renderExtras(GuiGraphics guiGraphics);

	void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles);

	JeiTooltip getTooltip(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper);

	boolean isVisible();

	default boolean handleClick(UserInput input, IInternalKeyMappings keyBindings, IRecipesGui recipesGui, FocusUtil focusUtil) {
		return false;
	}
}
