package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.renderer.Rect2i;

import java.util.function.Supplier;

public class OffsetJeiInputHandler implements IJeiInputHandler {
	private final IJeiInputHandler inputHandler;
	private final Supplier<ImmutablePoint2i> offset;

	public OffsetJeiInputHandler(IJeiInputHandler inputHandler, Supplier<ImmutablePoint2i> offset) {
		this.inputHandler = inputHandler;
		this.offset = offset;
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
		ImmutablePoint2i screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		Rect2i originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			double relativeMouseX = offsetMouseX - originalArea.getX();
			double relativeMouseY = offsetMouseY - originalArea.getY();
			return inputHandler.handleInput(relativeMouseX, relativeMouseY, input);
		}

		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
		ImmutablePoint2i screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		Rect2i originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			double relativeMouseX = offsetMouseX - originalArea.getX();
			double relativeMouseY = offsetMouseY - originalArea.getY();
			return inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaY);
		}

		return false;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		ImmutablePoint2i screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		Rect2i originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			double relativeMouseX = offsetMouseX - originalArea.getX();
			double relativeMouseY = offsetMouseY - originalArea.getY();
			return inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY);
		}

		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {
		ImmutablePoint2i screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		Rect2i originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			double relativeMouseX = offsetMouseX - originalArea.getX();
			double relativeMouseY = offsetMouseY - originalArea.getY();
			inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
		}
	}

	@Override
	public Rect2i getArea() {
		ImmutablePoint2i screenPosition = offset.get();
		Rect2i area = inputHandler.getArea();
		return new Rect2i(
			screenPosition.x() + area.getX(),
			screenPosition.y() + area.getY(),
			area.getWidth(),
			area.getHeight()
		);
	}
}
