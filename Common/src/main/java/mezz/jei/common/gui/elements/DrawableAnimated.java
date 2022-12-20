package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.util.TickTimer;

public class DrawableAnimated implements IDrawableAnimated {
	private final IDrawableStatic drawable;
	private final ITickTimer tickTimer;
	private final StartDirection startDirection;

	private static IDrawableAnimated.StartDirection invert(IDrawableAnimated.StartDirection startDirection) {
		return switch (startDirection) {
			case TOP -> StartDirection.BOTTOM;
			case BOTTOM -> StartDirection.TOP;
			case LEFT -> StartDirection.RIGHT;
			case RIGHT -> StartDirection.LEFT;
		};
	}

	public DrawableAnimated(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		final IDrawableAnimated.StartDirection animationStartDirection;
		if (inverted) {
			animationStartDirection = invert(startDirection);
		} else {
			animationStartDirection = startDirection;
		}

		int tickTimerMaxValue;
		if (animationStartDirection == IDrawableAnimated.StartDirection.TOP || animationStartDirection == IDrawableAnimated.StartDirection.BOTTOM) {
			tickTimerMaxValue = drawable.getHeight();
		} else {
			tickTimerMaxValue = drawable.getWidth();
		}
		this.drawable = drawable;
		this.tickTimer = new TickTimer(ticksPerCycle, tickTimerMaxValue, !inverted);
		this.startDirection = animationStartDirection;
	}

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
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		int maskLeft = 0;
		int maskRight = 0;
		int maskTop = 0;
		int maskBottom = 0;

		int animationValue = tickTimer.getValue();

		switch (startDirection) {
			case TOP -> maskBottom = animationValue;
			case BOTTOM -> maskTop = animationValue;
			case LEFT -> maskRight = animationValue;
			case RIGHT -> maskLeft = animationValue;
			default -> throw new IllegalStateException("Unknown startDirection " + startDirection);
		}

		drawable.draw(poseStack, xOffset, yOffset, maskTop, maskBottom, maskLeft, maskRight);
	}
}
