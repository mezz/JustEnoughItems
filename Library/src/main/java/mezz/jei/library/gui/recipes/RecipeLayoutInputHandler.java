package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeLayoutInputHandler<T> implements IJeiInputHandler, IInputTarget {
	private final RecipeLayout<T> recipeLayout;
	private final List<IJeiInputHandler> inputHandlers;
	private final List<IJeiGuiEventListener> guiEventListeners;

	public RecipeLayoutInputHandler(
		RecipeLayout<T> recipeLayout
	) {
		this.recipeLayout = recipeLayout;
		this.inputHandlers = new ArrayList<>();
		this.guiEventListeners = new ArrayList<>();
	}

	@Override
	public ScreenRectangle getArea() {
		Rect2i area = recipeLayout.getRect();
		return new ScreenRectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	/** Returns the interaction for the first recipe control that accepts the input. */
	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		for (IJeiInputHandler handler : inputHandlers) {
			ScreenRectangle area = getScreenArea(handler.getArea());
			if (!MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				continue;
			}
			if (handler instanceof IInputTarget target) {
				Optional<IInputInteraction> interaction = ApiInputAdapter.beginInputRelativeTo(screen, input, keys, target, () -> getScreenArea(handler.getArea()));
				if (interaction.isPresent()) {
					return interaction;
				}
			} else if (handler.handleInput(input.getMouseX() - area.left(), input.getMouseY() - area.top(), input)) {
				return Optional.of(createApiInteraction(input));
			}
		}
		for (IJeiGuiEventListener listener : guiEventListeners) {
			ScreenRectangle area = getScreenArea(listener.getArea());
			if (MathUtil.contains(area, input.getMouseX(), input.getMouseY()) &&
				handleInput(listener, input.getMouseX() - area.left(), input.getMouseY() - area.top(), input)
			) {
				return Optional.of(createApiInteraction(input));
			}
		}
		return Optional.empty();
	}

	/** Public callbacks check the controls under the pointer again when a click is released inside this recipe. */
	private IInputInteraction createApiInteraction(UserInput input) {
		if (input.isKeyboardInput()) {
			return ConsumedInput.INSTANCE;
		}
		return new InputAction(release -> handleInput(release.getMouseX(), release.getMouseY(), release));
	}

	private ScreenRectangle getScreenArea(ScreenRectangle controlArea) {
		Rect2i recipeArea = recipeLayout.getRect();
		return new ScreenRectangle(recipeArea.getX() + controlArea.left(), recipeArea.getY() + controlArea.top(), controlArea.width(), controlArea.height());
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJeiInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleInput(relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (handleInput(guiEventListener, relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean handleInput(IJeiGuiEventListener guiEventListener, double relativeMouseX, double relativeMouseY, IJeiUserInput userInput) {
		InputConstants.Key key = userInput.getKey();
		switch (key.getType()) {
			case MOUSE -> {
				if (userInput.isSimulate()) {
					return guiEventListener.mouseClicked(relativeMouseX, relativeMouseY, key.getValue());
				} else {
					return guiEventListener.mouseReleased(relativeMouseX, relativeMouseY, key.getValue());
				}
			}
			case KEYBOARD -> {
				if (!userInput.isSimulate()) {
					return guiEventListener.keyPressed(relativeMouseX, relativeMouseY, key.getValue(), 0, userInput.getModifiers());
				}
			}
			default -> {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJeiInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (guiEventListener.mouseDragged(relativeMouseX, relativeMouseY, mouseKey.getValue(), dragX, dragY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJeiInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaX, scrollDeltaY)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (guiEventListener.mouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaX, scrollDeltaY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJeiInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				guiEventListener.mouseMoved(relativeMouseX, relativeMouseY);
			}
		}
	}

	public void addInputHandler(IJeiInputHandler inputHandler) {
		this.inputHandlers.add(inputHandler);
	}

	public void addGuiEventListener(IJeiGuiEventListener guiEventListener) {
		this.guiEventListeners.add(guiEventListener);
	}
}
