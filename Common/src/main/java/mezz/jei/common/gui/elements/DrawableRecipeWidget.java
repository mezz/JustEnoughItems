package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IDrawableWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawableRecipeWidget extends AbstractRecipeWidgetBuilder<IDrawableWidget> implements IDrawableWidget {
	private final IDrawable drawable;

	public DrawableRecipeWidget(IDrawable drawable) {
		super(0, 0);
		this.drawable = drawable;
	}

	@Override
	protected IDrawableWidget getThis() {
		return this;
	}

	@Override
	public int getWidth() {
		return drawable.getWidth();
	}

	@Override
	public int getHeight() {
		return drawable.getHeight();
	}

	@Override
	public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		drawable.draw(guiGraphics);
	}
}
