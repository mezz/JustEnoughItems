package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;

public class DrawableAnimated implements IDrawableAnimated {
	private final IDrawableStatic drawable;
	private final ITickTimer tickTimer;
	private final StartDirection startDirection;

	public DrawableAnimated(IDrawableStatic drawable, int ticksPerCycle, StartDirection startDirection, boolean inverted) {
		this.drawable = drawable;

		if (inverted) {
			if (startDirection == StartDirection.TOP) {
				startDirection = StartDirection.BOTTOM;
			} else if (startDirection == StartDirection.BOTTOM) {
				startDirection = StartDirection.TOP;
			} else if (startDirection == StartDirection.LEFT) {
				startDirection = StartDirection.RIGHT;
			} else {
				startDirection = StartDirection.LEFT;
			}
		}
		this.startDirection = startDirection;

		int tickTimerMaxValue;
		if (startDirection == StartDirection.TOP || startDirection == StartDirection.BOTTOM) {
			tickTimerMaxValue = drawable.getHeight();
		} else {
			tickTimerMaxValue = drawable.getWidth();
		}
		this.tickTimer = JEIManager.guiHelper.createTickTimer(ticksPerCycle, tickTimerMaxValue, !inverted);
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
	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, 0, 0);
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset) {
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
		}

		drawable.draw(minecraft, xOffset, yOffset, maskTop, maskBottom, maskLeft, maskRight);
	}
}
