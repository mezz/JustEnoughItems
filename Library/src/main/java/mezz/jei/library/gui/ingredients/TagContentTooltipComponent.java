package mezz.jei.library.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class TagContentTooltipComponent<T> extends IngredientGridTooltipComponent<T> {
	private final IIngredientRenderer<T> renderer;

	public TagContentTooltipComponent(IIngredientRenderer<T> renderer, List<T> ingredients) {
		super(ingredients);
		this.renderer = renderer;
	}

	@Override
	protected void drawIngredient(GuiGraphics guiGraphics, T ingredient, int index, int x, int y, boolean hovered) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(x, y);
			this.renderer.render(guiGraphics, ingredient);
		}
		poseStack.popMatrix();
	}
}
