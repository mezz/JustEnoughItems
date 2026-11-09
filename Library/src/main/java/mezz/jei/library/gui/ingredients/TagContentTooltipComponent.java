package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.IJeiTooltipComponent;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.LazyMappedList;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class TagContentTooltipComponent extends IngredientGridTooltipComponent<ITypedIngredient<?>> implements IJeiTooltipComponent {
	private final IIngredientManager ingredientManager;

	public TagContentTooltipComponent(IIngredientManager ingredientManager, List<ITypedIngredient<?>> ingredients) {
		super(new LazyMappedList<>(ingredients, ingredientManager::normalizeTypedIngredient, MAX_VISIBLE_INGREDIENTS));
		this.ingredientManager = ingredientManager;
	}

	@Override
	public List<String> getText() {
		return List.of();
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients() {
		return getIngredientList();
	}

	@Override
	protected void drawIngredient(
		GuiGraphics guiGraphics,
		ITypedIngredient<?> ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	) {
		drawIngredient(guiGraphics, ingredient, x, y);
	}

	private <T> void drawIngredient(
		GuiGraphics guiGraphics,
		ITypedIngredient<T> ingredient,
		int x,
		int y
	) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientRenderer<T> renderer = this.ingredientManager.getIngredientRenderer(ingredientType);
		SafeIngredientUtil.render(guiGraphics, renderer, ingredient, x, y, IIngredientRenderContext.INGREDIENT);
	}
}
