package mezz.jei.gui.elements;

import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;

public class DrawableAnimated implements IDrawableAnimated {
	private final IDrawableStatic drawable;
	private final ITickTimer tickTimer;
	private final StartDirection startDirection;

	public DrawableAnimated(IDrawableStatic drawable, ITickTimer tickTimer, StartDirection startDirection) {
		this.drawable = drawable;
		this.tickTimer = tickTimer;
		this.startDirection = startDirection;
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
	public void draw(Minecraft minecraft) {
		draw(minecraft, 0, 0);
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		int maskLeft = 0;
		int maskRight = 0;
		int maskTop = 0;
		int maskBottom = 0;

		int animationValue = tickTimer.getValue();

		switch (startDirection) {
			case TOP:
				maskBottom = animationValue;
				break;
			case BOTTOM:
				maskTop = animationValue;
				break;
			case LEFT:
				maskRight = animationValue;
				break;
			case RIGHT:
				maskLeft = animationValue;
				break;
			default:
				throw new IllegalStateException("Unknown startDirection " + startDirection);
		}

		drawable.draw(minecraft, xOffset, yOffset, maskTop, maskBottom, maskLeft, maskRight);
	}
}
