package mezz.jei.gui.overlay.elements;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface IElement<T> {
	ITypedIngredient<T> getTypedIngredient();

	/**
	 * @return the bookmark if this element represents an existing bookmark.
	 */
	Optional<IBookmark> getBookmark();

	void renderExtras(GuiGraphics guiGraphics);

	void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles);

	List<Component> getTooltip(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper);

	default List<ClientTooltipComponent> getTooltipComponents(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper){
		return getTooltip(tooltipHelper, ingredientRenderer, ingredientHelper).stream()
			.map(component -> ClientTooltipComponent.create(component.getVisualOrderText()))
			.collect(Collectors.toList());
	}

	boolean isVisible();
}
