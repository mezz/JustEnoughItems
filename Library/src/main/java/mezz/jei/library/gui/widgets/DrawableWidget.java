package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.renderer.Rect2i;

public class DrawableWidget implements IRecipeWidget {
	private final IDrawable drawable;
	private final Rect2i area;

	public DrawableWidget(IDrawable drawable, int xPos, int yPos) {
		this.drawable = drawable;
		this.area = new Rect2i(xPos, yPos, drawable.getWidth(), drawable.getHeight());
	}

	@Override
	public Rect2i getArea() {
		return area;
	}

	@Override
	public void draw(PoseStack poseStack, double mouseX, double mouseY) {
		drawable.draw(poseStack);
	}
}
