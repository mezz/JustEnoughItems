package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

public class OffsetJeiInputHandler implements IJeiInputHandler {
	private final IJeiInputHandler inputHandler;
	private final Supplier<ScreenPosition> offset;

	public OffsetJeiInputHandler(IJeiInputHandler inputHandler, Supplier<ScreenPosition> offset) {
		this.inputHandler = inputHandler;
		this.offset = offset;
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleInput(relativeMouseX, relativeMouseY, input);
		}

		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaY);
		}

		return false;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY);
		}

		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
		}
	}

	@Override
	public ScreenRectangle getArea() {
		ScreenRectangle area = inputHandler.getArea();
		return new ScreenRectangle(offset.get(), area.width(), area.height());
	}
}
