package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.gui.IngredientGridTooltipComponent;

import java.util.List;

public class TagContentTooltipComponent<T> extends IngredientGridTooltipComponent<T> {
	private final IIngredientRenderer<T> renderer;

	public TagContentTooltipComponent(IIngredientRenderer<T> renderer, List<T> ingredients) {
		super(ingredients);
		this.renderer = renderer;
	}

	@Override
	protected void drawIngredient(PoseStack poseStack, T ingredient, int index, int x, int y, boolean hovered) {
		poseStack.pushPose();
		{
			poseStack.translate(x, y, 0);
			this.renderer.render(poseStack, ingredient);
		}
		poseStack.popPose();
	}
}
