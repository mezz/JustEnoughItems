package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class RecipeLayoutInputHandler<T> implements IJeiInputHandler {
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
				} else {
					return guiEventListener.mouseReleased(relativeMouseX, relativeMouseY, key.getValue());
				}
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
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
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
				if (inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDelta)) {
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
				if (guiEventListener.mouseScrolled(relativeMouseX, relativeMouseY, scrollDelta)) {
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
