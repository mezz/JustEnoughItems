package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.ILegacyRecipeInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.ConsumedInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeLayoutInputHandler<T> implements IJeiInputHandler, ILegacyRecipeInputTarget {
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

	/** Returns the interaction for the first recipe control that accepts the input. */
	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		for (IJeiInputHandler handler : inputHandlers) {
			Rect2i area = getScreenArea(handler.getArea());
			if (!MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
				continue;
			}
			if (handler instanceof IInputTarget target) {
				Optional<IInputInteraction> interaction = ApiInputAdapter.beginInputRelativeTo(screen, input, keys, target, () -> getScreenArea(handler.getArea()));
				if (interaction.isPresent()) {
					return interaction;
				}
			} else if (handler.handleInput(input.getMouseX() - area.getX(), input.getMouseY() - area.getY(), input)) {
				return Optional.of(createApiInteraction(input));
			}
		}
		for (IJeiGuiEventListener listener : guiEventListeners) {
			Rect2i area = getScreenArea(listener.getArea());
			if (MathUtil.contains(area, input.getMouseX(), input.getMouseY()) &&
				handleInput(listener, input.getMouseX() - area.getX(), input.getMouseY() - area.getY(), input)
			) {
				return Optional.of(createApiInteraction(input));
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IInputInteraction> beginLegacyInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		// The legacy category callback has no simulation flag, so execute it on release.
		if (input.isMouseInput()) {
			return Optional.of(new InputAction(this::handleLegacyCategoryInput));
		}
		if (handleLegacyCategoryInput(input)) {
			return Optional.of(ConsumedInput.INSTANCE);
		}
		return Optional.empty();
	}

	private boolean handleLegacyCategoryInput(UserInput input) {
		IRecipeCategory<T> category = recipeLayout.getRecipeCategory();
		Rect2i recipeArea = recipeLayout.getRect();
		@SuppressWarnings("removal")
		boolean handled = category.handleInput(recipeLayout.getRecipe(), input.getMouseX() - recipeArea.getX(), input.getMouseY() - recipeArea.getY(), input.getKey());
		return handled;
	}

	/** Public callbacks check the controls under the pointer again when a click is released inside this recipe. */
	private IInputInteraction createApiInteraction(UserInput input) {
		if (input.isKeyboardInput()) {
			return ConsumedInput.INSTANCE;
		}
		return new InputAction(release -> handleInput(release.getMouseX(), release.getMouseY(), release));
	}

	private Rect2i getScreenArea(Rect2i controlArea) {
		Rect2i recipeArea = recipeLayout.getRect();
		return new Rect2i(recipeArea.getX() + controlArea.getX(), recipeArea.getY() + controlArea.getY(), controlArea.getWidth(), controlArea.getHeight());
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
	public void resetInput() {
		for (IJeiInputHandler handler : inputHandlers) {
			if (handler instanceof IInputTarget target) {
				target.resetInput();
			}
		}
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
			Rect2i widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
				if (inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDelta)) {
					return true;
				}
			}
		}
		for (IJeiGuiEventListener guiEventListener : guiEventListeners) {
			Rect2i widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				double relativeMouseX = recipeMouseX - widgetArea.getX();
				double relativeMouseY = recipeMouseY - widgetArea.getY();
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
