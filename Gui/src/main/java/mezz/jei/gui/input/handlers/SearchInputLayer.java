package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class SearchInputLayer implements IGuiInputLayer, IRecipeFocusSource, IDragHandler {
	private final GuiTextFieldFilter textFieldFilter;
	private final BooleanSupplier active;
	private final IUserInputHandler textFieldInputHandler;

	public SearchInputLayer(GuiTextFieldFilter textFieldFilter, BooleanSupplier active) {
		this.textFieldFilter = textFieldFilter;
		this.active = active;
		this.textFieldInputHandler = textFieldFilter.createInputHandler();
	}

	@Override
	public void update(double mouseX, double mouseY) {
		if (active.getAsBoolean()) {
			textFieldFilter.updateCompletion((int) mouseX, (int) mouseY);
		} else {
			textFieldFilter.hideCompletion();
		}
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (active.getAsBoolean()) {
			textFieldFilter.drawCompletion(guiGraphics, mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return active.getAsBoolean() && textFieldFilter.isCompletionMouseOver(mouseX, mouseY);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		IGuiProperties guiProperties,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		if (!active.getAsBoolean()) {
			return Optional.empty();
		}
		if (textFieldFilter.isCompletionVisible() && (input.is(keyBindings.getEnterKey()) || input.is(keyBindings.getTabKey()))) {
			if (!input.isSimulate()) {
				textFieldFilter.acceptCompletion();
			}
			return Optional.of(this);
		}
		if (textFieldFilter.isCompletionVisible() && input.is(keyBindings.getPreviousSearch())) {
			if (!input.isSimulate()) {
				textFieldFilter.moveCompletion(-1);
			}
			return Optional.of(this);
		}
		if (textFieldFilter.isCompletionVisible() && input.is(keyBindings.getNextSearch())) {
			if (!input.isSimulate()) {
				textFieldFilter.moveCompletion(1);
			}
			return Optional.of(this);
		}
		if (textFieldFilter.isCompletionVisible() && input.is(keyBindings.getEscapeKey())) {
			if (!input.isSimulate()) {
				textFieldFilter.closeCompletion();
			}
			return Optional.of(this);
		}
		if (textFieldFilter.isCompletionVisible() && input.ifMouseEvent((event, doubleClicked) -> {
			if (input.isSimulate()) {
			return isMouseOver(event.x(), event.y());
			}
			return textFieldFilter.handleCompletionClick(event.x(), event.y());
			})
		) {
			return Optional.of(this);
		}
		return textFieldInputHandler.handleUserInput(screen, guiProperties, input, keyBindings);
	}

	@Override
	public void unfocus() {
		textFieldInputHandler.unfocus();
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public Optional<IDragHandler> handleDragStart(Screen screen, UserInput input) {
		if (isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public boolean handleDragComplete(Screen screen, UserInput input) {
		return true;
	}
}
