package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.screens.Screen;

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
	public Rect2i getArea() {
		return recipeLayout.getRect();
	}

	@Override
	public Optional<IUserInputHandler> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		Rect2i recipeArea = recipeLayout.getRect();
		double mouseX = input.getMouseX() - recipeArea.getX();
		double mouseY = input.getMouseY() - recipeArea.getY();
		for (IJeiInputHandler handler : inputHandlers) {
			if (handler instanceof IInputTarget target && MathUtil.contains(handler.getArea(), mouseX, mouseY)) {
				UserInput relativeInput = relativeInput(input, recipeLayout, handler);
				Optional<IUserInputHandler> accepted = target.beginInput(screen, relativeInput, keys);
				if (accepted.isPresent()) {
					return accepted.map(click -> new WidgetInputHandler(click, recipeLayout, handler));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public void resetInput() {
		for (IJeiInputHandler handler : inputHandlers) {
			if (handler instanceof IInputTarget target) {
				target.resetInput();
			}
		}
	}

	private static UserInput relativeInput(UserInput input, RecipeLayout<?> layout, IJeiInputHandler widget) {
		Rect2i recipeArea = layout.getRect();
		Rect2i position = widget.getArea();
		return new UserInput(input.getKey(),
			input.getMouseX() - recipeArea.getX() - position.getX(),
			input.getMouseY() - recipeArea.getY() - position.getY(),
			input.getModifiers(), input.getInputType());
	}

	private record WidgetInputHandler(IUserInputHandler click, RecipeLayout<?> layout, IJeiInputHandler widget) implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
			if (!layout.isMouseOver(input.getMouseX(), input.getMouseY())) {
				click.unfocus();
				return Optional.of(this);
			}
			return click.handleUserInput(screen, relativeInput(input, layout, widget), keys);
		}

		@Override
		public void unfocus() {
			click.unfocus();
		}
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
			Rect2i widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (inputHandler.handleInput(relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			Rect2i widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (handleInput(guiEventListener, relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}

		if (userInput.isSimulate()) {
			return true;
		}
		IRecipeCategory<T> recipeCategory = recipeLayout.getRecipeCategory();
		T recipe = recipeLayout.getRecipe();
		@SuppressWarnings("removal")
		boolean legacyResult = recipeCategory.handleInput(recipe, recipeMouseX, recipeMouseY, userInput.getKey());
		return legacyResult;
	}

	private static boolean handleInput(IJeiGuiEventListener guiEventListener, double relativeMouseX, double relativeMouseY, IJeiUserInput userInput) {
		InputConstants.Key key = userInput.getKey();
		switch (key.getType()) {
			case MOUSE -> {
				if (userInput.isSimulate()) {
					return guiEventListener.mouseClicked(relativeMouseX, relativeMouseY, key.getValue());
				}
				return guiEventListener.mouseReleased(relativeMouseX, relativeMouseY, key.getValue());
			}
			case KEYSYM -> {
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
			Rect2i widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			Rect2i widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (guiEventListener.mouseDragged(relativeMouseX, relativeMouseY, mouseKey.getValue(), dragX, dragY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJeiInputHandler inputHandler : inputHandlers) {
			Rect2i widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaY)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			Rect2i widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (guiEventListener.mouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaY)) {
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
			Rect2i widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			Rect2i widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
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
