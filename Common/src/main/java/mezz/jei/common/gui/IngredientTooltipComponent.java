package mezz.jei.common.gui;

import mezz.jei.api.gui.IJeiTooltipComponent;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class IngredientTooltipComponent<T> implements ClientTooltipComponent, IJeiTooltipComponent {
	private static final int INGREDIENT_PADDING = 1;

	private final ITypedIngredient<T> typedIngredient;
	private final IIngredientRenderer<T> ingredientRenderer;

	public IngredientTooltipComponent(ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer) {
		this.typedIngredient = typedIngredient;
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public List<String> getText() {
		return List.of();
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients() {
		return List.of(typedIngredient);
	}

	@Override
	public int getHeight(Font font) {
		return ingredientRenderer.getHeight() + (2 * INGREDIENT_PADDING);
	}

	@Override
	public int getWidth(Font font) {
		return ingredientRenderer.getWidth() + (2 * INGREDIENT_PADDING);
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
		SafeIngredientUtil.render(
			guiGraphics,
			ingredientRenderer,
			typedIngredient,
			x + INGREDIENT_PADDING,
			y + INGREDIENT_PADDING,
			IIngredientRenderContext.INGREDIENT
		);
	}
}
