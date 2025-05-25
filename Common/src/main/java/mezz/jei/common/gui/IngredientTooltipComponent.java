package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class IngredientTooltipComponent<T> implements ClientTooltipComponent, TooltipComponent {
	private static final int INGREDIENT_PADDING = 1;

	private final ITypedIngredient<T> typedIngredient;
	private final IIngredientRenderer<T> ingredientRenderer;

	public IngredientTooltipComponent(ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer) {
		this.typedIngredient = typedIngredient;
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public int getHeight() {
		return ingredientRenderer.getHeight() + (2 * INGREDIENT_PADDING);
	}

	@Override
	public int getWidth(Font font) {
		return ingredientRenderer.getWidth() + (2 * INGREDIENT_PADDING);
	}

	@Override
	public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int z) {
		SafeIngredientUtil.render(
			poseStack,
			ingredientRenderer,
			typedIngredient,
			x + INGREDIENT_PADDING,
			y + INGREDIENT_PADDING
		);
	}
}
