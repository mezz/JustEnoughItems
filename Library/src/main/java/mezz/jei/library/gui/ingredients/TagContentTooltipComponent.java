package mezz.jei.library.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class TagContentTooltipComponent extends IngredientGridTooltipComponent<ITypedIngredient<?>> {
	private final IIngredientManager ingredientManager;

	public TagContentTooltipComponent(IIngredientManager ingredientManager, List<ITypedIngredient<?>> ingredients) {
		super(ingredients);
		this.ingredientManager = ingredientManager;
	}

	@Override
	protected void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
		ITypedIngredient<?> ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	) {
		drawIngredient(guiGraphics, ingredient, x, y);
	}

	private <T> void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
		ITypedIngredient<T> ingredient,
		int x,
		int y
	) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientRenderer<T> renderer = this.ingredientManager.getIngredientRenderer(ingredientType);
		SafeIngredientUtil.render(guiGraphics, renderer, ingredient, x, y);
	}
}
